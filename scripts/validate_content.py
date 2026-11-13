from pathlib import Path
import subprocess,sys,tempfile
root=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as temp:
    subprocess.run([sys.executable,'content-source/build.py',temp],cwd=root,check=True)
    generated=list(Path(temp).glob('*.json'))
    if not generated: raise SystemExit('No generated assets')
    for p in generated:
        if p.read_bytes()!=(root/'app/src/main/assets'/p.name).read_bytes():
            raise SystemExit('Stale packaged asset: '+p.name)
print('Packaged JSON matches deterministic regeneration.',flush=True)
subprocess.run([sys.executable,'-m','unittest','discover','-s','content-source/tests','-v'],cwd=root,check=True)
