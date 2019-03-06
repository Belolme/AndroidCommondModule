uniform mat4 uMatrix;
uniform float uSize;
uniform vec2 uScreenSize;

attribute vec2 aPosition;

varying vec2 vCenter;
varying float vRadius;

void main() {
    vec4 t = uMatrix * vec4(aPosition, 0.0, 1.0);
    vCenter = vec2((t.x / t.w + 1.0) * uScreenSize.x / 2.0, (t.y / t.w + 1.0) * uScreenSize.y / 2.0);
    vRadius = uSize / 2.0;
    gl_Position = t;
    gl_PointSize = uSize + 2.0; // 1 is antialise width
}
