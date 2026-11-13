from pathlib import Path
import json,sys,hashlib
root=Path(__file__).resolve().parents[1]
doc=json.loads((root/'docs/release-acceptance.json').read_text())
failed=[]
for name,item in doc['gates'].items():
    if item.get('status')!='passed':
        failed.append(name+': '+item.get('status','missing')); continue
    if not item.get('evidence'): failed.append(name+': missing evidence')
    for e in item.get('evidence',[]):
        p=(root/e['path']).resolve()
        if not p.is_relative_to(root) or not p.is_file() or not p.stat().st_size:
            failed.append(name+': unavailable evidence'); continue
        if hashlib.sha256(p.read_bytes()).hexdigest()!=e.get('sha256'):
            failed.append(name+': evidence hash mismatch')
for lesson in doc['lessons']:
    if lesson.get('acceptance')!='passed': failed.append(lesson['id']+': lesson acceptance pending')
if failed:
    print('RELEASE BLOCKED: '+str(len(failed))+' unmet conditions')
    print('\n'.join(failed)); sys.exit(2)
print('All recorded gates passed with hash-verified evidence.')
