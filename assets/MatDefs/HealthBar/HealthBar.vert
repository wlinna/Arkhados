uniform mat4 g_WorldViewProjectionMatrix;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

varying vec2 texCoord;

void main() {
    texCoord = inTexCoord;
    vec3 inPos = inPosition;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPos, 1.0); 
}