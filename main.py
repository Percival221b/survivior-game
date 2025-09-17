from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch
import re

# =======================
# 模型加载
# =======================
model_name = "Qwen/Qwen1.5-1.8B-Chat"
tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained(model_name, device_map="auto")


# =======================
# 请求体
# =======================
class ChatRequest(BaseModel):
    message: str


# =======================
# FastAPI app
# =======================
app = FastAPI(title="Qwen Action Extraction Service")


@app.post("/chat")
async def chat(req: ChatRequest):
    try:
        # 构造 prompt
        prompt = f"USER: {req.message}\nAI:"

        inputs = tokenizer(prompt, return_tensors="pt").to(model.device)
        outputs = model.generate(
            **inputs,
            max_new_tokens=500,  # 增加生成长度
            do_sample=True,
            temperature=0.7,
            top_p=0.9,
            pad_token_id=tokenizer.eos_token_id
        )

        response_text = tokenizer.decode(outputs[0], skip_special_tokens=True)
        answer_text = response_text[len(prompt):].strip()  # 去掉 prompt

        # =======================
        # 提取动作或目标
        # =======================
        # 支持 patrol / attack / flee
        matches = re.findall(r"\b(patrol|attack|flee)\b", answer_text.lower())

        # 如果没匹配到，再尝试 hero / monster
        if not matches:
            matches = re.findall(r"\b(hero|monster)\b", answer_text.lower())

        action = matches[-1] if matches else None

        return {
            "answer": action,
            "raw_output": response_text
        }

    except Exception as e:
        return {"answer": None, "raw_output": str(e)}


# =======================
# 启动服务
# =======================
if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8001)
