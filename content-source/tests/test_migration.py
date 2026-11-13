import copy,hashlib,ipaddress,json,sys,unittest
from collections import Counter
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'content-source'))
from migrate_lessons import apply_migration,validate_migration,STAGES
from migration_authoring import LESSON_DESIGNS
class MigrationTests(unittest.TestCase):
    def setUp(self):
        self.c=json.loads((ROOT/'app/src/main/assets/curriculum.json').read_text())
        self.l=self.c['lessons']
        self.d={d['id'] for d in json.loads((ROOT/'app/src/main/assets/diagrams.json').read_text())['diagrams']}
    def test_all_41_migrated(self):
        self.assertEqual(41,len(self.l));self.assertTrue(all(len(l['learningStages'])==8 for l in self.l))
    def test_39_authored_scenarios(self): self.assertEqual(39,len(LESSON_DESIGNS))
    def test_schema(self): self.assertTrue(validate_migration(self.l,self.d))
    def test_reproducible_composition(self):
        again=copy.deepcopy(self.l);apply_migration(again);self.assertEqual(self.l,again)
    def test_legacy_fingerprints(self):
        hashes=json.loads((ROOT/'content-source/legacy_lesson_fingerprints.json').read_text())
        for l in self.l:
            legacy={k:v for k,v in l.items() if k!='learningStages'}
            h=hashlib.sha256(json.dumps(legacy,sort_keys=True,separators=(',',':'),ensure_ascii=False).encode()).hexdigest()
            self.assertEqual(hashes[l['id']],h,l['id'])
    def test_original_depth_present_in_new_flow(self):
        for l in self.l:
            items=[x for s in l['learningStages'] for b in s['blocks'] for x in b.get('items',[])]
            for sec in l['deep']:self.assertIn(sec['text'],items,l['id'])
    def test_original_examples_present(self):
        for l in self.l:
            items=[x for s in l['learningStages'] for b in s['blocks'] for x in b.get('items',[])]
            for ex in l['examples']:self.assertIn(ex['text'],items,l['id'])
    def test_original_terminology_present(self):
        for l in self.l:
            rows=[row for s in l['learningStages'] for b in s['blocks'] for row in b.get('rows',[])]
            for t in l['terms']:self.assertIn([t['term'],t['definition']],rows)
    def test_no_tier_metadata_in_stages(self):
        for l in self.l:
            for s in l['learningStages']:
                for b in s['blocks']:self.assertNotIn('level',b)
    def test_all_41_interactive_checks(self):
        checks=[b for l in self.l for s in l['learningStages'] for b in s['blocks'] if b['type']=='check']
        self.assertEqual(41,len(checks))
    def test_answer_positions_varied(self):
        counts=Counter(b['correctIndex'] for l in self.l for s in l['learningStages'] for b in s['blocks'] if b['type']=='check')
        self.assertEqual({0,1,2,3},set(counts))
    def test_rationale_rotation(self):
        for l in self.l:
            if l['id'] not in LESSON_DESIGNS:continue
            d=LESSON_DESIGNS[l['id']]
            q=next(b for s in l['learningStages'] for b in s['blocks'] if b['type']=='check')
            self.assertEqual(d['options'][d['correct']],q['items'][q['correctIndex']])
            self.assertEqual(dict(zip(d['options'],d['rationales'])),dict(zip(q['items'],q['rationales'])))
    def test_no_unexplained_empty_stage(self):
        for l in self.l:
            for s in l['learningStages']:self.assertTrue(s['blocks'] if s['applicable'] else s['reason'].strip())
    def test_invalid_diagram_rejected(self):
        self.l[0]['learningStages'][2]['blocks'][0]['diagram']='absent'
        with self.assertRaises(AssertionError):validate_migration(self.l,self.d)
    def test_duplicate_block_rejected(self):
        self.l[0]['learningStages'][0]['blocks'][1]['id']=self.l[0]['learningStages'][0]['blocks'][0]['id']
        with self.assertRaises(AssertionError):validate_migration(self.l,self.d)
    def test_missing_rationale_rejected(self):
        q=next(b for l in self.l for s in l['learningStages'] for b in s['blocks'] if b['type']=='check');q['rationales']=[]
        with self.assertRaises(AssertionError):validate_migration(self.l,self.d)
    def test_subnet_fixture_nonoverlap(self):
        nets=[ipaddress.ip_network(n) for n in ['192.168.10.0/26','192.168.10.64/27','192.168.10.96/28']]
        self.assertEqual([62,30,14],[n.num_addresses-2 for n in nets])
        self.assertTrue(all(not a.overlaps(b) for i,a in enumerate(nets) for b in nets[i+1:]))
    def test_json_fixture_parses(self):
        d=LESSON_DESIGNS['6-03'];self.assertEqual({'vlans':[10,20,30],'enabled':True},json.loads(d['config'].split(': ',1)[1]))
    def test_original_question_ids_unique(self):
        ids=[q['id'] for l in self.l for q in l['quiz']];self.assertEqual(len(ids),len(set(ids)))
    def test_all_domain_coverage(self):self.assertEqual(6,len({l['domainId'] for l in self.l}))
if __name__=='__main__':unittest.main()
