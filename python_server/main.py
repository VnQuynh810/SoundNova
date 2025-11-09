from spleeter.separator import Separator
from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from concurrent.futures import ThreadPoolExecutor
import requests
import whisper
import os
from zipfile import ZipFile
from fastapi.responses import FileResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
import shutil

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"],  
    allow_headers=["*"], 
)

app.mount("/static", StaticFiles(directory="."), name="static")

# Request model
class AudioRequest(BaseModel):
    url: str  
    ngrok_url: str

def clean_output_dir(output_dir):
    if os.path.exists(output_dir):
        shutil.rmtree(output_dir) 
    os.makedirs(output_dir, exist_ok=True) 

def download_audio(url, output_path="audio.mp3"):
    try:
        response = requests.get(url)
        if response.status_code == 200:
            with open(output_path, "wb") as file:
                file.write(response.content)
            return output_path
        else:
            raise Exception(f"Failed to download audio. Status code: {response.status_code}")
    except Exception as e:
        raise Exception(f"Error downloading audio: {str(e)}")

def extract_karaoke(input_path, output_dir="output"):
    separator = Separator('spleeter:2stems')  
    separator.separate_to_file(input_path, output_dir)

def audio_to_text(input_path):
    model = whisper.load_model("base") 
    result = model.transcribe(input_path)
    return result["text"]

def create_zip(output_dir="output"):
    zip_filename = "karaoke_output.zip"
    with ZipFile(zip_filename, 'w') as zipf:
        vocal_path = os.path.join(output_dir, "audio", "vocals.wav")
        if os.path.exists(vocal_path):
            zipf.write(vocal_path, "vocals.wav")

        karaoke_path = os.path.join(output_dir, "audio", "accompaniment.wav")
        if os.path.exists(karaoke_path):
            zipf.write(karaoke_path, "karaoke_track.wav")
    return zip_filename

@app.post("/process-audio")
async def process_audio(audio_request: AudioRequest, background_tasks: BackgroundTasks):
    url = audio_request.url
    ngrok_url = audio_request.ngrok_url

    audio_path = download_audio(url)
    print(audio_path)
    output_dir = "output"
    clean_output_dir(output_dir)

    # background_tasks.add_task(extract_karaoke, audio_path, output_dir)
    # transcription = audio_to_text(audio_path)
    
    with ThreadPoolExecutor() as executor:
        extract_future = executor.submit(extract_karaoke, audio_path)
        transcription_future = executor.submit(audio_to_text, audio_path)
        # Wait for extract to complete
        extract_future.result()  # Block until finished
        # Transcription can run in parallel and doesn't block the main thread
        transcription = transcription_future.result()
 
    zip_file = create_zip()
    zip_file_url = f"{ngrok_url}/static/{os.path.basename(zip_file)}"
    return {"message": "Processing Complete", "transcription": transcription, "zip_file": zip_file_url}

