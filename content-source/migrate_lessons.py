"""Compose all 41 lessons reproducibly while retaining every legacy field."""
import copy,json
from pathlib import Path
from migration_authoring import LESSON_DESIGNS
STAGES=['Understand','Compare','Visualize','Observe','Configure','Verify','Troubleshoot','Recall']
ROOT=Path(__file__).resolve().parent

def block(id,title,text,type='text',**kwargs):
    return dict(id=id,type=type,title=title,text=text,**kwargs)
def detail(id,title,text):
    return block(id,title,'Open this explanation when you are ready for the full mechanism.','detail',items=[text])
def make_stage(name,blocks,applicable=True,reason=''):
    return dict(id=name.lower(),title=name,applicable=applicable,reason=reason,blocks=blocks)
def apply_migration(lessons):
    pilots=json.loads((ROOT/'pilot_lessons.json').read_text())['lessons']
    assert set(LESSON_DESIGNS)=={l['id'] for l in lessons}-set(pilots)
    for l in lessons:
        lid=l['id']
        if lid in pilots:
            stages=copy.deepcopy(pilots[lid])
            for st in stages:
                for i,b in enumerate(st['blocks']): b['id']=f'{lid}-{st["id"]}-pilot-{i+1:02d}'
        else:
            d=LESSON_DESIGNS[lid]
            cfg=[]
            if d['config']:
                cfg.append(block(f'{lid}-configure-context','Fixture, command context, and restoration',d['config_note']))
                if d['config'].startswith('GUI WORKFLOW:'):
                    cfg.append(block(f'{lid}-configure-workflow','GUI workflow, not terminal commands',d['config']))
                elif d['config'].startswith('JSON DATA EXAMPLE:'):
                    cfg.append(block(f'{lid}-configure-data','JSON data, not terminal commands','Authored data example; no live API is called.','code',items=[d['config'].split(': ',1)[1]]))
                else:
                    cfg.append(block(f'{lid}-configure-commands','Isolated lab configuration','Instructional IOS configuration, not a live device connection. Verify the stated platform and fixture before use.','code',items=d['config'].splitlines()))
            shift=int(lid.replace('-',''))%4
            pairs=list(zip(d['options'],d['rationales']))
            pairs=pairs[shift:]+pairs[:shift]
            correct=(d['correct']-shift)%4
            stages=[
                make_stage('Understand',[block(f'{lid}-understand-scenario','Start with the problem',d['opening']),block(f'{lid}-understand-mechanism','Build the mental model',d['mechanism'])]),
                make_stage('Compare',[block(f'{lid}-compare-choices','Separate the similar ideas',d['compare'])]),
                make_stage('Visualize',[block(f'{lid}-visualize-reading','Read the relationships',d['visual'])]+[block(f'{lid}-visualize-{dg}','Existing conceptual illustration','This bundled illustration supports the concept; exact scenario-specific diagrams remain subject to visual review.','visual',diagram=dg) for dg in l['diagrams']]),
                make_stage('Observe',[block(f'{lid}-observe-sequence','Follow the behavior',d['observe'])]),
                make_stage('Configure',cfg,bool(d['config']),d['no_config']),
                make_stage('Verify',[block(f'{lid}-verify-evidence','What evidence should show',d['verify'])]),
                make_stage('Troubleshoot',[block(f'{lid}-troubleshoot-scenario','Diagnose, correct, and retest',d['troubleshoot'])]),
                make_stage('Recall',[block(f'{lid}-recall-scenario','Apply the idea',d['question'],'check',items=[x[0] for x in pairs],correctIndex=correct,rationales=[x[1] for x in pairs])])]
        # Preserve original depth inside progressive disclosure, not learner-level tiers.
        st={x['title']:x for x in stages}
        st['Understand']['blocks'].append(detail(f'{lid}-reference-simple','Revisit the core explanation',l['simple']))
        st['Understand']['blocks'].append(detail(f'{lid}-reference-why','Why this matters in practice',l['why']))
        for i,sec in enumerate(l['deep']):
            st['Understand']['blocks'].append(detail(f'{lid}-reference-deep-{i}',sec['heading'],sec['text']))
        st['Compare']['blocks'].append(block(f'{lid}-reference-terms','Use the technical vocabulary','Read the term and explain its role before checking the definition.','chunks',rows=[[t['term'],t['definition']] for t in l['terms']]))
        for i,ex in enumerate(l['examples']):
            st['Observe']['blocks'].append(detail(f'{lid}-reference-example-{i}',ex['title'],ex['text']))
        st['Troubleshoot']['blocks'].append(block(f'{lid}-reference-mistakes','Mistakes to avoid','Use these checks when the evidence does not fit your first hypothesis.','bullets',items=l['mistakes']))
        st['Recall']['blocks'].extend([
            block(f'{lid}-reference-summary','Explain it in your own words',l['summary']),
            block(f'{lid}-reference-memorize','Facts worth retrieving from memory','Use these as retrieval prompts, not a replacement for the mechanism.','bullets',items=l['memorize']),
            block(f'{lid}-reference-exam','Exam interpretation','Original objective-linked exam notes are retained.','bullets',items=l['examNotes'])])
        if l['supplementary']:
            st['Recall']['blocks'].append(detail(f'{lid}-reference-supplementary','Beyond the exam: supplementary context','\n\n'.join(l['supplementary'])))
        from interactive_authoring import PACKET, OSPF_PACKET, OUTPUTS, PILOT_CHECKS
        if lid in ('1-03','1-05','1-06','3-01','4-01'):
            st['Observe']['blocks'].append(block(f'{lid}-packet-web','Follow the Packet','Inspect the authored topology and headers step by step.','packet',packet=copy.deepcopy(PACKET)))
        if lid=='3-03':
            st['Observe']['blocks'].append(block(f'{lid}-packet-ospf','Inspect OSPF progression','Follow a representative successful adjacency and inspect selected fields.','packet',packet=copy.deepcopy(OSPF_PACKET)))
        if lid in PILOT_CHECKS:
            q=PILOT_CHECKS[lid]
            st['Recall']['blocks'].append(block(f'{lid}-interactive-check','Apply the pilot',q['question'],'check',items=q['items'],correctIndex=q['correctIndex'],rationales=q['rationales']))
        for output in OUTPUTS:
            if output['lessonId']==lid:
                st['Verify']['blocks'].append(block(f'{lid}-inspect-{output["id"]}',output['command'],'Compare healthy and abnormal evidence in a declared fixture.','inspector',inspector=copy.deepcopy(output)))
        from configuration_expansions import EXPANSIONS
        for expansion in EXPANSIONS.get(lid,[]):
            target=st[expansion['stage']]
            target['applicable']=True
            target['reason']=''
            target['blocks'].append(copy.deepcopy(expansion['block']))
        from mechanism_diagrams import mechanism_lessons
        if lid in mechanism_lessons():
            st['Visualize']['blocks'].insert(0,block(f'{lid}-mechanism-diagram','Inspect the mechanism','Read the explanation for alternatives and assumptions; arrows are reading guides where stated.','visual',diagram='mechanism_'+lid))
        fixture_diagram={'2-01':'vlan_pilot_fixture','3-03':'ospf_pilot_fixture'}.get(lid)
        if fixture_diagram:
            st['Visualize']['blocks'].insert(0,block(f'{lid}-exact-topology','Exact command fixture','Use this topology for the named ports and addresses; other illustrations remain conceptual.','visual',diagram=fixture_diagram))
        l['learningStages']=stages
    validate_migration(lessons)

def validate_migration(lessons,diagram_ids=None):
    seen=set()
    for l in lessons:
        stages=l['learningStages']
        assert [s['title'] for s in stages]==STAGES,l['id']
        assert len({s['id'] for s in stages})==8
        for s in stages:
            assert s['applicable'] or s['reason'].strip()
            assert s['blocks'] or not s['applicable']
            for b in s['blocks']:
                assert b['id'] and b['id'] not in seen,b['id']
                seen.add(b['id'])
                assert b['type'] in {'text','visual','code','detail','check','chunks','bullets','packet','inspector','table'}
                assert b['title'].strip() and b['text'].strip()
                if diagram_ids is not None and b.get('diagram'): assert b['diagram'] in diagram_ids
                if b['type'] in {'code','detail','bullets'}: assert b['items']
                if b['type']=='packet':
                    packet=b['packet']
                    assert packet['devices'] and packet['steps'] and packet['assumptions']
                    if diagram_ids is not None: assert packet['diagramId'] in diagram_ids
                    names={d['name'] for d in packet['devices']}
                    assert len({x['id'] for x in packet['steps']})==len(packet['steps'])
                    for step in packet['steps']:
                        assert step['device'] in names
                        assert step['condition'] in {'always','dnsMiss','gatewayMiss'}
                        assert all(step[k].strip() for k in ['action','why','next','interface'])
                if b['type']=='inspector':
                    output=b['inspector']
                    assert all(output[k].strip() for k in ['fixture','healthy','abnormal','question','answer','provenance'])
                    assert 'simulated' in output['provenance'].lower()
                    assert output['fields']
                if b['type']=='check':
                    assert len(b['items'])==len(b['rationales'])==4
                    assert 0<=b['correctIndex']<4
                    assert all(x.strip() for x in b['rationales'])
    return True

if __name__=='__main__':
    root=ROOT.parent
    c=json.loads((root/'app/src/main/assets/curriculum.json').read_text())
    d=json.loads((root/'app/src/main/assets/diagrams.json').read_text())
    validate_migration(c['lessons'],{x['id'] for x in d['diagrams']})
    print('PASS: all lesson stages, stable block IDs, applicability, questions, and diagram references')
