
uniform float g_Time;
uniform float m_Speed;
uniform sampler2D m_Color;
uniform sampler2D m_Noise;

#ifdef ALPHA
uniform float m_Alpha;
#endif

varying vec2 texCoord;

void main() {
    
    vec2 d = texCoord;

    d.x += cos(g_Time * 0.02) * m_Speed;
    d.y += sin(g_Time * 0.04) * m_Speed;

    float factor = 1.0 - 2.0 * abs(d.y - 0.5);

    vec4 noise = texture2D(m_Noise, -d);

    vec2 d2 = texCoord;
    //d2.x += tan(noise.r);
    //d2.y += tan(noise.r);

    d2.x += noise.r;
    d2.y += noise.r;

    vec4 color = texture2D(m_Color, d2);
#ifdef ALPHA
    color.w = m_Alpha;
#endif
    gl_FragColor = color;
}