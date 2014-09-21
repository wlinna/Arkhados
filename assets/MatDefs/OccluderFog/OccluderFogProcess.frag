uniform sampler2D m_FogShape;

varying vec2 texCoord;

void main() {
    // Set the fragment color for example to gray, alpha 1.0
      vec4 texVal = texture2D(m_FogShape, texCoord);
if (texVal.a > 0.2) {
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.2);
} else {
discard;
}
}