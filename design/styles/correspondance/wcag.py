def L(h):
    h=h.lstrip('#'); c=[int(h[i:i+2],16)/255 for i in (0,2,4)]
    c=[x/12.92 if x<=0.04045 else ((x+0.055)/1.055)**2.4 for x in c]
    return 0.2126*c[0]+0.7152*c[1]+0.0722*c[2]
def r(a,b):
    x,y=sorted([L(a),L(b)]); return (y+0.05)/(x+0.05)
import sys
for l in sys.stdin:
    p=l.split()
    if p: print(f"{p[0]:>10} {p[1]} on {p[2]:>10} {p[3]}: {r(p[1],p[3]):.2f}")
