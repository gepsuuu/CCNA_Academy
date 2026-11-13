import copy,ipaddress,json,sys,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'content-source'))
from interactive_authoring import PACKET,OSPF_PACKET,OUTPUTS,PC,R1L,R1T,R2T,R2L,WEB,CLIENT,SERVER
from configuration_expansions import EXPANSIONS,A,B
from migrate_lessons import validate_migration
class InteractiveTests(unittest.TestCase):
 def setUp(self):
  self.c=json.loads((ROOT/'app/src/main/assets/curriculum.json').read_text())
  self.d=json.loads((ROOT/'app/src/main/assets/diagrams.json').read_text())['diagrams']
  self.steps={s['id']:s for s in PACKET['steps']}
 def test_step_ids_unique(self):self.assertEqual(len(PACKET['steps']),len(self.steps))
 def test_no_live_claim(self):self.assertIn('not captured traffic',PACKET['assumptions'])
 def test_dns_route_before_query(self):
  ids=list(self.steps);self.assertLess(ids.index('dns-route'),ids.index('dns-query'));self.assertLess(ids.index('dns-arp-reply'),ids.index('dns-query'))
 def test_arp_is_not_ip(self):
  for s in PACKET['steps']:
   if s['headers'].get('EtherType')=='ARP':self.assertNotIn('TTL',s['headers']);self.assertNotIn('Protocol',s['headers'])
 def test_remote_gateway_not_server_arp(self):self.assertEqual('192.168.10.1',self.steps['gateway-arp-request']['headers']['ARP target IPv4'])
 def test_ip_endpoints_unchanged(self):
  for prefix in ['SYN','ACK','HTTP request']:
   for i in range(3):
    h=self.steps[f'{prefix}-{i}']['headers'];self.assertEqual(CLIENT,h['Source IPv4']);self.assertEqual(SERVER,h['Destination IPv4'])
 def test_ttl_per_routed_hop(self):
  for prefix in ['SYN','SYN-ACK','ACK','HTTP request','HTTP response']:self.assertEqual([64,63,62],[int(self.steps[f'{prefix}-{i}']['headers']['TTL']) for i in range(3)])
 def test_mac_rewrite(self):
  self.assertEqual([(PC,R1L),(R1T,R2T),(R2L,WEB)],[(self.steps[f'SYN-{i}']['headers']['Source MAC'],self.steps[f'SYN-{i}']['headers']['Destination MAC']) for i in range(3)])
 def test_switch_preserves_header(self):self.assertEqual(self.steps['SYN-0']['headers'],self.steps['switch-forward']['headers'])
 def test_handshake_acknowledges_syn(self):
  self.assertEqual('1001',self.steps['SYN-ACK-0']['headers']['Acknowledgment']);self.assertEqual('5001',self.steps['ACK-0']['headers']['Acknowledgment'])
 def test_http_response_sequence_arithmetic(self):
  req=self.steps['HTTP request-0']['headers'];resp=self.steps['HTTP response-0']['headers']
  self.assertEqual(req['Acknowledgment'],resp['Sequence'])
  self.assertEqual(int(req['Sequence'])+120,int(resp['Acknowledgment']))
  self.assertIn('120 bytes',req['Payload'])
  ids=list(self.steps);self.assertLess(ids.index('HTTP request-2'),ids.index('HTTP response-0'));self.assertEqual('application',ids[-1])
 def test_reverse_mac_rewrite(self):
  self.assertEqual([(WEB,R2L),(R2T,R1T),(R1L,PC)],[(self.steps[f'HTTP response-{i}']['headers']['Source MAC'],self.steps[f'HTTP response-{i}']['headers']['Destination MAC']) for i in range(3)])
 def test_reverse_ip_path(self):
  for i in range(3):
   self.assertEqual(SERVER,self.steps[f'HTTP response-{i}']['headers']['Source IPv4'])
   h=self.steps[f'SYN-ACK-{i}']['headers'];self.assertEqual(SERVER,h['Source IPv4']);self.assertEqual(CLIENT,h['Destination IPv4'])
 def test_cache_branches(self):
  cold=PACKET['steps'];warm=[s for s in cold if s['condition']=='always'];self.assertEqual(7,len(cold)-len(warm));self.assertIn('SYN-0',[s['id'] for s in warm])
 def test_router_cache_assumption_explicit(self):self.assertIn('prepopulated',self.steps['router-cache-assumption']['action'])
 def test_ipv4_addresses_valid(self):
  for s in PACKET['steps']:
   for k,v in s['headers'].items():
    if k in ['Source IPv4','Destination IPv4','ARP sender IPv4','ARP target IPv4']:ipaddress.IPv4Address(v)
 def test_ports_in_range(self):
  for s in PACKET['steps']:
   for k in ['Source port','Destination port']:
    if k in s['headers']:self.assertTrue(0<int(s['headers'][k])<=65535)
 def test_ten_output_cases(self):self.assertEqual(10,len(OUTPUTS))
 def test_output_pairs_differ(self):
  for o in OUTPUTS:self.assertNotEqual(o['healthy'],o['abnormal']);self.assertIn('simulated',o['provenance'])
 def test_outputs_have_reasoning(self):
  for o in OUTPUTS:self.assertTrue(o['fields']);self.assertTrue(o['question']);self.assertTrue(o['answer'])
 def test_packet_diagram_references(self):
  ids={d['id'] for d in self.d}
  for p in [PACKET,OSPF_PACKET]:self.assertIn(p['diagramId'],ids)
 def test_new_diagram_bounds(self):
  for d in self.d:
   if not (d['id'].endswith('_fixture') or d['id'].startswith('mechanism_')):continue
   for n in d['nodes']:self.assertTrue(0<=n['x'] and 0<=n['y'] and n['x']+n['w']<=d['width'] and n['y']+n['h']<=d['height'],(d['id'],n['id']))
 def test_new_diagram_nodes_do_not_overlap(self):
  for d in self.d:
   if not (d['id'].endswith('_fixture') or d['id'].startswith('mechanism_')):continue
   for i,a in enumerate(d['nodes']):
    for b in d['nodes'][i+1:]:self.assertFalse(a['x']<b['x']+b['w'] and b['x']<a['x']+a['w'] and a['y']<b['y']+b['h'] and b['y']<a['y']+a['h'])
 def test_expansion_context_for_each_code(self):
  for lid,entries in EXPANSIONS.items():
   for e in entries:
    b=e['block']
    if b['type']=='code':
     stem=b['id'].removesuffix('-commands');self.assertTrue(any(x['block']['id']==stem+'-context' for x in entries));self.assertTrue(any(x['block']['id']==stem+'-undo' for x in entries))
 def test_pinout_pairs(self):
  self.assertEqual([A[0],A[1],A[2],A[5]],['white/green','green','white/orange','orange']);self.assertEqual([B[0],B[1],B[2],B[5]],['white/orange','orange','white/green','green'])
 def test_ospf_types(self):
  text=json.dumps(OSPF_PACKET);self.assertIn('89 (not TCP/UDP)',text);self.assertIn('5 Link State Acknowledgment',text);self.assertIn('11 = transit 10 + remote loopback 1',text)
 def test_all_lesson_practice(self):
  for l in self.c['lessons']:self.assertTrue(any(b['type']=='check' for s in l['learningStages'] for b in s['blocks']),l['id'])
 def test_new_models_have_defaults(self):
  text=(ROOT/'app/src/main/java/com/ccnaacademy/app/data/MemoryModels.kt').read_text();self.assertIn('val packet: PacketWalkthrough? = null',text);self.assertIn('val inspector: OutputInspection? = null',text)
 def test_highlight_resolver_has_old_and_new_keys(self):
  # Resolver moved from the Compose screen to the JVM-testable data layer in 1.5.0-rc2 (DECISION_LOG D-05).
  text=(ROOT/'app/src/main/java/com/ccnaacademy/app/data/HighlightText.kt').read_text();self.assertIn('section.startsWith("block:")',text);self.assertIn('section.startsWith("deep")',text)
 def test_no_unknown_json_helper(self):
  for p in (ROOT/'app/src/test').rglob('*.kt'):self.assertNotIn('JsonCodec',p.read_text())
 def test_cli_engine_unchanged_from_manifest_baseline(self):
  # Preservation is checked against the separately recorded original hashes, not inferred from file names.
  import hashlib
  baseline=json.loads((ROOT/'docs/protected-source-hashes.json').read_text())
  for rel,digest in baseline.items():self.assertEqual(digest,hashlib.sha256((ROOT/rel).read_bytes()).hexdigest(),rel)
 def test_invalid_packet_reference_rejected(self):
  lessons=copy.deepcopy(self.c['lessons']);q=next(b for l in lessons for s in l['learningStages'] for b in s['blocks'] if b['type']=='packet');q['packet']['diagramId']='missing'
  with self.assertRaises(AssertionError):validate_migration(lessons,{d['id'] for d in self.d})
if __name__=='__main__':unittest.main()

class ReleaseGateTests(unittest.TestCase):
 def test_unverified_release_is_blocked(self):
  import subprocess
  result=subprocess.run([sys.executable,str(ROOT/'scripts/acceptance_gate.py')],capture_output=True,text=True)
  self.assertEqual(2,result.returncode);self.assertIn('RELEASE BLOCKED',result.stdout)
 def test_all_lessons_have_visual_reference(self):
  c=json.loads((ROOT/'app/src/main/assets/curriculum.json').read_text())
  for l in c['lessons']:self.assertTrue(any(b.get('diagram') or b.get('packet') for s in l['learningStages'] for b in s['blocks']),l['id'])
