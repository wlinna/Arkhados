uniform mat4 g_WorldViewProjectionMatrix;

attribute vec4 inPosition;
attribute vec2 inTexCoord;

varying vec2 texCoord;

void main() { 
    // Vertex transformation 

texCoord = inTexCoord;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition.xyz, 1.0); 
}
