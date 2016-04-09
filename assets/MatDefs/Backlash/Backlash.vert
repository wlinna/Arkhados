attribute vec3 inPosition;

uniform mat4 g_WorldViewProjectionMatrix;

varying vec3 pos;

void main(void) {
   // compute position
   pos = inPosition.xzy;
   gl_Position = g_WorldViewProjectionMatrix * vec4(pos, 1.0);
   //pos = gl_Position.xyz;
} 
