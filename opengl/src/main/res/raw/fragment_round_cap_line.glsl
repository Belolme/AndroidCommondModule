precision mediump float;

uniform vec4 uColor;

varying vec2  vp0, vp1, vp;
varying float vThickness;

void main() {
//    gl_FragColor = uColor;

    float d = 0.0;
    float capWidth = vThickness / 2.0;
    if (vp.x < vp0.x) {
        d = length(vp - vp0) - capWidth;
    } else if(vp.x > vp1.x) {
        d = length(vp - vp1) - capWidth;
    } else {
        d = abs(vp.y) - capWidth;
    }

    if(d < 0.0) {
        gl_FragColor = uColor;
    } else {
        gl_FragColor = vec4(uColor.r, uColor.g, uColor.b, exp(-d * d) * uColor.a);
    }
}
