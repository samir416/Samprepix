const fs = require('fs');
const path = require('path');

const srcDir = 'd:/AI-Placement-Platform/frontend/src';

function processFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');
    if (!content.includes('http://localhost:8080')) return;

    // Calculate relative path for import
    const depth = filePath.replace(/\\/g, '/').split('/').length - srcDir.replace(/\\/g, '/').split('/').length;
    let importPrefix = depth === 1 ? './' : '../'.repeat(depth - 1);
    
    if (!content.includes('import { API_BASE_URL }')) {
        content = `import { API_BASE_URL } from "${importPrefix}config";\n` + content;
    }

    // Replace strings
    content = content.replace(/"http:\/\/localhost:8080([^"]*)"/g, 'API_BASE_URL + "$1"');
    content = content.replace(/'http:\/\/localhost:8080([^']*)'/g, "API_BASE_URL + '$1'");
    // Replace template literals
    content = content.replace(/`http:\/\/localhost:8080([^`]*)`/g, '`${API_BASE_URL}$1`');

    fs.writeFileSync(filePath, content, 'utf-8');
    console.log('Updated ' + filePath);
}

function scanDir(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            scanDir(fullPath);
        } else if (fullPath.endsWith('.js') || fullPath.endsWith('.jsx')) {
            processFile(fullPath);
        }
    }
}

scanDir(srcDir);
