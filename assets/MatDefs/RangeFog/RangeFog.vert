uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform vec3 m_PlayerPosition;

attribute vec3 inPosition;

varying vec4 characterPos;
varying vec4 vertexWorldPos;


void main() {
    vec4 pos = vec4(inPosition, 1.0);
    vertexWorldPos  = g_WorldMatrix * pos;
    characterPos = vec4(m_PlayerPosition.x, vertexWorldPos.y, m_PlayerPosition.z, 1.0);

    gl_Position = g_WorldViewProjectionMatrix * pos;
}
