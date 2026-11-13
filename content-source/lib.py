import json
LESSONS=[]
def L(id, domain, chapter, title, obj, minutes, simple, deep, why, examples, mistakes, terms, memorize, exam, summary, quiz, diagrams=(), supp=(), required=True):
    LESSONS.append(dict(
        id=id, domainId=domain, chapterId=chapter, title=title, objectives=list(obj),
        examRequired=required, minutes=minutes, simple=simple,
        deep=[dict(level=l, heading=h, text=t) for l,h,t in deep],
        why=why, examples=[dict(title=a, text=b) for a,b in examples],
        mistakes=list(mistakes), terms=[dict(term=a, definition=b) for a,b in terms],
        memorize=list(memorize), examNotes=list(exam), supplementary=list(supp),
        summary=summary, diagrams=list(diagrams),
        quiz=[dict(id=f"{id}-q{i+1}", question=q, options=o, answer=a, explanation=e, lessonId=id, domainId=domain) for i,(q,o,a,e) in enumerate(quiz)]))
B="Beginner"; I="Intermediate"; X="CCNA exam level"
