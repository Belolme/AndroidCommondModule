uniform mat4 uMatrix;
uniform float uSize;

attribute vec2 ap;

void main() {
    gl_Position = uMatrix * vec4(ap, 0, 1);
    gl_PointSize = uSize;
}
