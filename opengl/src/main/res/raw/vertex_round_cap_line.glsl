uniform mat4 uMatrix;
uniform float uAntialias;
uniform float uThickness;

attribute vec2 ap0, ap1, auv;

varying vec2  vp0, vp1, vp;
varying float vThickness;

void main() {

    float w = uAntialias * 2.0 + uThickness;
    float capWidth = w / 2.0;

    float lineLength = length(ap1 - ap0);

    vec2 t = normalize(ap1 - ap0);
    vec2 o = vec2(-t.y, t.x);

    vec2 ap;
    if(auv.x == -1.0) ap = ap0;
    else ap = ap1;

    gl_Position = uMatrix * vec4((auv.x * t * capWidth + auv.y * o * capWidth + ap), 0, 1);

    vp0 = vec2(0.0, 0.0);
    vp1 = vec2(lineLength, 0.0);

    t = vec2(1, 0);
    o = vec2(0, 1);

    if(auv.x == -1.0) ap = vp0;
    else ap = vp1;

    vp = auv.x * t * capWidth + auv.y * o * capWidth + ap;
    vThickness = uThickness;
}
