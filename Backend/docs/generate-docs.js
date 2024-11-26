const { exec } = require('child_process');
const fs = require('fs');

// Configuración básica
const config = {
    springAppPort: 8080,
    projectName: "Mi API",
    outputDir: 'docs'
};

// Función principal
async function generateDocs() {
    console.log('📚 Iniciando generación de documentación...');

    // 1. Crear estructura de carpetas
    createDirectoryStructure();

    // 2. Crear configuración básica de Mintlify
    createMintConfig();

    // 3. Generar documentación
    await generateDocumentation();
}

// Crear estructura de carpetas necesaria
function createDirectoryStructure() {
    const directories = [
        config.outputDir,
        `${config.outputDir}/pages`,
        `${config.outputDir}/api`
    ];

    directories.forEach(dir => {
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir, { recursive: true });
            console.log(`📁 Creado directorio: ${dir}`);
        }
    });
}

// Crear configuración de Mintlify
function createMintConfig() {
    const mintConfig = {
        name: config.projectName,
        "colors": {
            "primary": "#0099FF",
            "light": "#66C2FF",
            "dark": "#006BB3"
        },
        "navigation": [
            {
                "group": "Guía",
                "pages": ["introduccion"]
            },
            {
                "group": "API",
                "pages": ["api/endpoints"]
            }
        ],
        "api": {
            "baseUrl": `http://localhost:${config.springAppPort}`
        }
    };

    fs.writeFileSync(
        `${config.outputDir}/mint.json`,
        JSON.stringify(mintConfig, null, 2)
    );
    console.log('📄 Configuración de Mintlify creada');
}

// Generar documentación
async function generateDocumentation() {
    const commands = [
        // Generar especificación OpenAPI
        `curl http://localhost:${config.springAppPort}/v3/api-docs > ${config.outputDir}/openapi.yaml`,
        // Iniciar Mintlify
        'npx mintlify dev'
    ];

    for (const command of commands) {
        try {
            console.log(`🚀 Ejecutando: ${command}`);
            await executeCommand(command);
        } catch (error) {
            console.error(`❌ Error: ${error.message}`);
        }
    }
}

// Ejecutar comandos
function executeCommand(command) {
    return new Promise((resolve, reject) => {
        exec(command, (error, stdout, stderr) => {
            if (error) {
                reject(error);
                return;
            }
            console.log(stdout);
            resolve();
        });
    });
}

// Ejecutar el script
generateDocs().catch(console.error);