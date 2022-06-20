#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal; 
out vec3 vNormal;					  
out vec3 vVertPos;					  
out vec3 varyingLightDir;
out vec3 varyingHalfVector;
out vec4 shadow_coord;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;		
uniform mat4 shadowMVP;
layout (binding = 0) uniform samplerCube t;	//Uses cubemap texture as this texture 
layout (binding = 1) uniform sampler2DShadow shadowTex;
out float wantLights;

struct PositionalLight
{	
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};
uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform float choice;

void main(void)
{
	vVertPos = (mv_matrix * vec4(position,1.0)).xyz; 
	varyingLightDir = light.position - vVertPos;
	vNormal = (norm_matrix * vec4(normal,1.0)).xyz;  
	varyingHalfVector =
		normalize(normalize(varyingLightDir)
		+ normalize(-vVertPos)).xyz;
	
	shadow_coord = shadowMVP * vec4(vVertPos, 1.0);
	
	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
	
	wantLights = choice; //Behave differently depending on day/night scene
}
