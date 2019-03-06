uniform mat4 uMatrix;
uniform float uSize;

attribute vec2 ap;
attribute vec4 aColor;

varying vec4 vColor;

void main() {
    vColor = aColor;
    gl_Position = uMatrix * vec4(ap, 0, 1);
    gl_PointSize = uSize;
}
