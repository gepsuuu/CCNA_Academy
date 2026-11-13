import copy,json,sys,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'content-source'))
from validate_pilots import validate
class PilotTests(unittest.TestCase):
    def setUp(self):
        self.p=json.loads((ROOT/'content-source/pilot_lessons.json').read_text())
        self.c=json.loads((ROOT/'app/src/main/assets/curriculum.json').read_text())
        self.d=json.loads((ROOT/'app/src/main/assets/diagrams.json').read_text())
        self.ids=[l['id'] for l in self.c['lessons']]
        self.diagrams=[d['id'] for d in self.d['diagrams']]
    def check(self): return validate(self.p,self.ids,self.diagrams)
    def test_valid(self): self.assertTrue(self.check())
    def test_bad_lesson(self):
        self.p['lessons']['missing']=self.p['lessons'].pop('2-01')
        with self.assertRaises(AssertionError): self.check()
    def test_bad_diagram(self):
        self.p['lessons']['2-01'][2]['blocks'][0]['diagram']='missing'
        with self.assertRaises(AssertionError): self.check()
    def test_missing_stage(self):
        self.p['lessons']['2-01'].pop()
        with self.assertRaises(AssertionError): self.check()
    def test_unexplained_not_applicable(self):
        self.p['lessons']['2-01'][0]['applicable']=False
        with self.assertRaises(AssertionError): self.check()
    def test_explained_not_applicable(self):
        s=self.p['lessons']['2-01'][0];s['applicable']=False;s['reason']='Test reason'
        self.assertTrue(self.check())
    def test_empty_stage(self):
        self.p['lessons']['2-01'][0]['blocks']=[]
        with self.assertRaises(AssertionError): self.check()
    def test_assets_match_authoring(self):
        byid={l['id']:l for l in self.c['lessons']}
        for lid,stages in self.p['lessons'].items():
            actual=byid[lid]['learningStages']
            for source,target in zip(stages,actual):
                self.assertEqual(source['title'],target['title'])
                for index,authored in enumerate(source['blocks']):
                    rendered=next(b for b in target['blocks'] if b['id']==f'{lid}-{source["id"]}-pilot-{index+1:02d}')
                    self.assertEqual(authored,{k:v for k,v in rendered.items() if k!='id'})
    def test_scope_remains_two_pilots(self): self.assertEqual({'2-01','3-03'},set(self.p['lessons']))
    def test_output_provenance(self):
        for stages in self.p['lessons'].values():
            for s in stages:
                if s['title']=='Verify':
                    for b in s['blocks']:
                        if b['type']=='code': self.assertIn('simulated',b['text'].lower())
if __name__=='__main__': unittest.main()
