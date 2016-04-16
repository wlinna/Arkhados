
uniform float m_Health;
uniform float m_HealthLowRecord;
uniform sampler2D m_HealthTex;
uniform sampler2D m_HealTex;

varying vec2 texCoord;

void main() {
    if (texCoord.x > m_HealthLowRecord && texCoord.x <= m_Health) {
       gl_FragColor = texture2D(m_HealTex, texCoord);
    } else if (texCoord.x <= m_Health) {
       gl_FragColor = texture2D(m_HealthTex, texCoord);
    } else {
       gl_FragColor = vec4(0.1, 0.1, 0.1, 1.0);
    }
}