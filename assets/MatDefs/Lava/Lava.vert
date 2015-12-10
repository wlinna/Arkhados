uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;

varying vec2 texCoord;
attribute vec2 inTexCoord;

void main() {
    texCoord = inTexCoord;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0); 
}