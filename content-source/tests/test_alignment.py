"""Semantic lesson-to-diagram alignment.

Reference validity (the diagram ID exists) does not prove the diagram teaches the lesson it is placed in.
These checks require lesson-specific concepts in each lesson-specific diagram and verify that, of all 41 lessons,
the assigned lesson is the best vocabulary match for the diagram. Run with CCNA_ASSET_DIR=<dir> to audit another
asset set (used as a negative control against the 1.5.0-rc1 package, which fails these checks).
"""
import json, os, re, unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = Path(os.environ.get('CCNA_ASSET_DIR', ROOT / 'app/src/main/assets'))

# Concepts a learner must be able to see in the diagram for that lesson (lower-case substrings, all required).
REQUIRED = {
    '1-01': ['switch', 'router', 'access point', 'firewall'],
    '1-02': ['spine', 'leaf', 'core', 'distribution'],
    '1-04': ['copper', 'fiber'],
    '1-07': ['binary', 'hex', 'nibble', '128'],
    '1-08': ['rfc1918', '10.0.0.0/8', '172.16.0.0/12', '192.168.0.0/16', 'nat'],
    '1-11': ['ipconfig', 'gateway', 'resolver'],
    '1-13': ['hypervisor', 'container', 'vrf'],
    '2-04': ['cdp', 'lldp'],
    '2-05': ['lacp', 'port-channel', 'hash'],
    '2-07': ['capwap', 'controller', 'local switching', 'central'],
    '2-08': ['ssid', 'wlan', 'vlan', 'catalyst 9800'],
    '2-09': ['ssh', 'vty', 'login local', 'console', 'transport input ssh'],
    '4-03': ['stratum', 'ntp'],
    '5-01': ['threat', 'vulnerability', 'risk'],
    '5-02': ['authentication', 'authorization', 'accounting', 'radius', 'tacacs+'],
    '6-01': ['northbound', 'southbound', 'controller', 'data plane'],
    '6-02': ['predictive', 'generative', 'model'],
    '6-04': ['ansible', 'terraform', 'inventory', 'playbook', 'plan', 'apply', 'state'],
}
# Lessons that must never receive a given topic's diagram (regression guard for the rc1 off-by-one placement).
FORBIDDEN = {
    '1-07': ['rfc1918'], '2-08': ['capwap control to wlc'], '2-09': ['ssid'],
    '6-02': ['northbound'], '6-04': ['model inference'],
}
STOP = set('a an and are as at be by can for from in into is it its not of on or the to this with without what which '
           'when where does do each only than that then their these they use used using your you one two only must may'.split())


def stem(w):
    return w[:-1] if len(w) > 4 and w.endswith('s') and not w.endswith('ss') else w


def words(text):
    return {stem(w) for w in re.findall(r'[a-z0-9][a-z0-9+./-]*[a-z0-9+]|[a-z0-9]', text.lower()) if w not in STOP and len(w) > 2}


def diagram_text(d):
    parts = [d['title'], d.get('explanation', '')]
    parts += [n['label'] + ' ' + n.get('sub', '') for n in d['nodes']]
    parts += [e.get('label', '') for e in d['edges']]
    return ' '.join(parts).lower()


def lesson_vocab(l):
    parts = [l['title'], l['simple'], l['why'], ' '.join(t['term'] + ' ' + t['definition'] for t in l['terms'])]
    # Authored stage prose (not diagram blocks, so the diagram cannot vote for itself).
    parts += [b['text'] + ' ' + ' '.join(x for x in b.get('items', []) if isinstance(x, str))
              for s in l['learningStages'] for b in s['blocks'] if b['type'] in ('text', 'detail', 'bullets')]
    return words(' '.join(parts))


class AlignmentTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.c = json.loads((ASSETS / 'curriculum.json').read_text())
        cls.d = {x['id']: x for x in json.loads((ASSETS / 'diagrams.json').read_text())['diagrams']}
        cls.placements = {}
        for l in cls.c['lessons']:
            for s in l['learningStages']:
                for b in s['blocks']:
                    if b.get('diagram', '').startswith('mechanism_'):
                        cls.placements.setdefault(b['diagram'], set()).add(l['id'])

    def test_mechanism_diagram_placed_only_in_its_own_lesson(self):
        for did, lessons in self.placements.items():
            self.assertEqual({did.removeprefix('mechanism_')}, lessons, did)

    def test_every_mechanism_has_required_concepts(self):
        missing = {}
        for lid, concepts in REQUIRED.items():
            d = self.d.get('mechanism_' + lid)
            self.assertIsNotNone(d, lid)
            text = diagram_text(d)
            gaps = [c for c in concepts if c not in text]
            if gaps: missing[lid] = gaps
        self.assertEqual({}, missing)

    def test_every_mechanism_diagram_has_a_concept_contract(self):
        self.assertEqual(set(REQUIRED), {k.removeprefix('mechanism_') for k in self.d if k.startswith('mechanism_')})

    def test_forbidden_cross_topic_content(self):
        for lid, bad in FORBIDDEN.items():
            text = diagram_text(self.d['mechanism_' + lid])
            for b in bad: self.assertNotIn(b, text, lid)

    def test_assigned_lesson_is_best_vocabulary_match(self):
        vocab = {l['id']: lesson_vocab(l) for l in self.c['lessons']}
        wrong = {}
        for lid in REQUIRED:
            dw = words(diagram_text(self.d['mechanism_' + lid]))
            scores = sorted(((len(dw & v) / (len(v) ** 0.5), k) for k, v in vocab.items()), reverse=True)
            if scores[0][1] != lid: wrong[lid] = [k for _, k in scores[:3]]
        self.assertEqual({}, wrong)

    def test_lesson_title_concept_visible_in_diagram(self):
        for lid in REQUIRED:
            title = next(l['title'] for l in self.c['lessons'] if l['id'] == lid)
            tw = words(title) - {'network', 'concept', 'fundamental', 'their', 'role'}
            self.assertTrue(tw & words(diagram_text(self.d['mechanism_' + lid])), (lid, title))

    def test_custom_edges_are_labelled(self):
        for lid in ('1-07', '2-09', '6-04'):
            for e in self.d['mechanism_' + lid]['edges']:
                self.assertTrue(e['label'] and e['label'] != 'read next', (lid, e))


if __name__ == '__main__':
    unittest.main()
