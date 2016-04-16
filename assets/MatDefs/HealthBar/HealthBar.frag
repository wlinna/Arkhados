
uniform float m_Health;
uniform sampler2D m_HealthTex;
varying vec2 texCoord;

void main() {
    if (texCoord.x <= m_Health) {
       gl_FragColor = texture2D(m_HealthTex, texCoord);
    } else {
       discard;
    }
}