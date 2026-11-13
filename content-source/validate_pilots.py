"""Structural checks, not a substitute for technical or device review."""
import json
from pathlib import Path
STAGES = ['Understand','Compare','Visualize','Observe','Configure','Verify','Troubleshoot','Recall']
def validate(pilot, lesson_ids, diagram_ids):
    assert pilot['blueprintVersion'] == 'v1.1'
    assert set(pilot['lessons']) <= set(lesson_ids)
    for lid, stages in pilot['lessons'].items():
        assert [s['title'] for s in stages] == STAGES, lid
        assert len({s['id'] for s in stages}) == 8, lid
        for stage in stages:
            if not stage['applicable']:
                assert stage['reason'].strip(), lid
                continue
            assert stage['blocks'], (lid, stage['title'])
            for block in stage['blocks']:
                assert block['type'] in {'text','visual','code','detail'}
                assert block['title'].strip() and block['text'].strip()
                if block.get('diagram'): assert block['diagram'] in diagram_ids
                if block['type'] in {'code','detail'}: assert block['items']
    return True
if __name__ == '__main__':
    root=Path(__file__).resolve().parents[1]
    c=json.loads((root/'app/src/main/assets/curriculum.json').read_text())
    d=json.loads((root/'app/src/main/assets/diagrams.json').read_text())
    p=json.loads(Path(__file__).with_name('pilot_lessons.json').read_text())
    validate(p, [l['id'] for l in c['lessons']], [x['id'] for x in d['diagrams']])
    print('PASS: pilot stage structure, content fields, and diagram references')
