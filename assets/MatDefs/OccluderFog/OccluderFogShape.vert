uniform mat4 g_WorldMatrix;
uniform mat4 g_ViewProjectionMatrix;
uniform mat4 g_WorldViewProjectionMatrix;
uniform mat3 g_WorldMatrixInverseTranspose;

uniform vec3 m_PlayerPosition;

attribute vec4 inPosition;
attribute vec3 inNormal;

varying float r;
varying float g;
varying float b;

varying vec4 varyingPos;

void main() {
  vec4 pos = vec4(inPosition.xyz, 1.0);
  vec4 vertexWorldPos  = g_WorldMatrix * vec4(pos.xyz, 1.0);

  if (inPosition.w == 0.0) {
    gl_Position = g_WorldViewProjectionMatrix * pos;
    varyingPos = inPosition;
    g = 1.0;
    return;
  }

   vec3 characterPos = vec3(m_PlayerPosition.x, vertexWorldPos.y, m_PlayerPosition.z);

   vec3 fromCharacterToVertex = normalize(vertexWorldPos.xyz - characterPos);

   vec3 worldNormal3 = normalize(g_WorldMatrixInverseTranspose * inNormal);

   float dotRes = dot(worldNormal3, fromCharacterToVertex);

   if (dotRes > 0.0) {
      vec4 movedPos = vec4(vertexWorldPos.xyz + fromCharacterToVertex * 400.0, 1.0);
      varyingPos = movedPos;
      gl_Position = g_ViewProjectionMatrix * movedPos;
   } else {
     gl_Position = g_ViewProjectionMatrix * (g_WorldMatrix * pos);
   }

//   r = abs(inNormal.x);
  // g = dotRes;
   //b = abs(inNormal.z);
}
