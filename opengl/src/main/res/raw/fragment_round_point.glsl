precision mediump float;

uniform vec4 uColor;

varying vec2 vCenter;
varying float vRadius;

void main() {
    // convert to android coordination
    vec2 fragCoord = vec2(gl_FragCoord.x, gl_FragCoord.y);
    float d = length(fragCoord - vCenter) - vRadius;

    if(d < 0.0) {
        gl_FragColor = uColor;
    } else {
        gl_FragColor = vec4(uColor.r, uColor.g, uColor.b, exp(-d * d));
    }

//    float dist = length(gl_PointCoord - vec2(0.5));
//    float value = -smoothstep(0.48, 0.5, dist) + 1.0;
//    if (value == 0.0) {
//        discard;
//    }
//    gl_FragColor = uColor;
}
