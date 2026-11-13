"""Documentation must state the required teaching pipeline exactly: no omitted Compare, no substituted Review, no reordering."""
import re, unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PIPELINE = ['Understand', 'Compare', 'Visualize', 'Observe', 'Configure', 'Verify', 'Troubleshoot', 'Recall']
WORDS = PIPELINE + ['Review', 'Practice', 'Apply', 'Explore']
LIST = re.compile(r'\b(?:%s)\b(?:\s*(?:→|->|,|, and|and)\s*\b(?:%s)\b){4,}' % ('|'.join(WORDS), '|'.join(WORDS)))


def documents():
    yield from (ROOT / 'docs').glob('*.md')
    for name in ('README.md', 'CHANGELOG.md'):
        if (ROOT / name).exists(): yield ROOT / name


class PipelineDocumentationTests(unittest.TestCase):
    def test_every_stage_list_is_the_exact_pipeline(self):
        bad = []
        for doc in documents():
            for m in LIST.finditer(doc.read_text()):
                stages = re.findall(r'\b(?:%s)\b' % '|'.join(WORDS), m.group(0))
                # A run that starts with Understand is a pipeline statement; it must be exactly the eight stages.
                if stages[0] == 'Understand' and stages != PIPELINE:
                    bad.append((doc.name, m.group(0)[:120]))
        self.assertEqual([], bad)

    def test_system_design_states_pipeline(self):
        self.assertIn(' → '.join(PIPELINE), (ROOT / 'docs/SYSTEM_DESIGN.md').read_text())

    def test_code_stage_order_matches(self):
        src = (ROOT / 'content-source/migrate_lessons.py').read_text()
        self.assertIn("STAGES=" + repr(PIPELINE).replace(' ', ''), src.replace(' ', ''))


if __name__ == '__main__':
    unittest.main()
