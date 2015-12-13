uniform float g_Time;
uniform mat4 g_WorldViewProjectionMatrix;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

varying vec2 texCoord;

void main() {
    texCoord = inTexCoord;
    vec3 inPos = inPosition;
    //inPos.y += 0.2 * sin(g_Time+inPosition.y) * inPosition.y; 
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPos, 1.0); 
}