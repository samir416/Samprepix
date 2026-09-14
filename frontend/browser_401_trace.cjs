/**
 * REAL BROWSER 401 DIAGNOSIS
 * 
 * Creates a new account in a REAL headed browser, captures every 
 * network request, and traces the exact token flow to find why
 * PUT /api/profile returns 401.
 */
const puppeteer = require('puppeteer');

const FRONTEND = 'http://localhost:5173';
const U = Date.now();
const EMAIL = `browser401_${U}@test.com`;
const USER  = `browser401_${U}`;
const PASS  = 'Test1234!';

(async () => {
    console.log('=== REAL BROWSER 401 TRACE ===');
    console.log(`Email: ${EMAIL}`);
    
    const browser = await puppeteer.launch({
        headless: false,
        args: ['--no-sandbox', '--disable-setuid-sandbox'],
        defaultViewport: { width: 1280, height: 900 },
    });
    
    const page = await browser.newPage();
    
    // Capture ALL network requests and responses 
    const networkLog = [];
    page.on('request', req => {
        const url = req.url();
        if (url.includes('localhost:8080')) {
            const entry = {
                url: url,
                method: req.method(),
                headers: req.headers(),
                time: new Date().toISOString(),
            };
            networkLog.push(entry);
            console.log(`  → ${req.method()} ${url}`);
            if (entry.headers.authorization) {
                const auth = entry.headers.authorization;
                console.log(`    Authorization: ${auth.substring(0, 40)}...${auth.substring(auth.length - 20)}`);
            }
        }
    });
    
    page.on('response', async (res) => {
        const url = res.url();
        if (url.includes('localhost:8080')) {
            const status = res.status();
            console.log(`  ← ${status} ${url}`);
            if (status === 401) {
                let body = '';
                try { body = await res.text(); } catch (_) {}
                console.log(`    *** 401 BODY: ${body} ***`);
            }
        }
    });
    
    // Also capture console logs from the page
    page.on('console', msg => {
        const text = msg.text();
        if (text.includes('401') || text.includes('token') || text.includes('error') || text.includes('unauthorized') || text.includes('Unable')) {
            console.log(`  [PAGE CONSOLE] ${text}`);
        }
    });
    
    try {
        // Step 1: Navigate to /auth (Create Account page)
        console.log('\n--- Step 1: Navigate to /auth ---');
        await page.goto(`${FRONTEND}/auth`, { waitUntil: 'networkidle2', timeout: 15000 });
        console.log('  Current URL:', page.url());
        
        // Step 2: Fill in registration form
        console.log('\n--- Step 2: Fill registration form ---');
        await page.waitForSelector('input[placeholder="John Doe"]', { timeout: 5000 });
        
        await page.type('input[placeholder="John Doe"]', 'Browser Test', { delay: 30 });
        await page.type('input[placeholder="john416"]', USER, { delay: 30 });
        await page.type('input[placeholder="john@example.com"]', EMAIL, { delay: 30 });
        await page.type('input[placeholder="••••••••"]', PASS, { delay: 30 });
        
        // Step 3: Submit registration
        console.log('\n--- Step 3: Submit registration ---');
        await page.click('button.auth-submit-btn');
        
        // Wait for navigation to /onboarding
        console.log('  Waiting for navigation...');
        await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 }).catch(() => {});
        await new Promise(r => setTimeout(r, 2000));
        
        const urlAfterReg = page.url();
        console.log('  URL after registration:', urlAfterReg);
        
        // Dump localStorage
        const lsToken = await page.evaluate(() => localStorage.getItem('token'));
        const lsUser = await page.evaluate(() => localStorage.getItem('user'));
        const lsOb = await page.evaluate(() => localStorage.getItem('onboardingCompleted'));
        console.log('  localStorage.token:', lsToken ? `${lsToken.substring(0, 30)}...(len=${lsToken.length})` : 'NULL');
        console.log('  localStorage.user:', lsUser ? lsUser.substring(0, 100) : 'NULL');
        console.log('  localStorage.onboardingCompleted:', lsOb);
        
        if (!urlAfterReg.includes('/onboarding')) {
            console.log('  ERROR: Did not navigate to /onboarding! Something went wrong during registration.');
            // Take a screenshot to see what happened
            await page.screenshot({ path: `C:\\Users\\HP\\.gemini\\antigravity\\brain\\6ea10720-0c8c-4923-bf6a-0b2138f5cf94\\scratch\\reg_fail_screenshot.png` });
            console.log('  Screenshot saved.');
            
            // Check if there's an error message visible
            const errorText = await page.evaluate(() => {
                const el = document.querySelector('.auth-error-alert .auth-error-content p');
                return el ? el.textContent : null;
            });
            if (errorText) console.log('  Visible error:', errorText);
            
            await browser.close();
            return;
        }
        
        // Step 4: Complete onboarding
        console.log('\n--- Step 4: Complete onboarding (Step 1 - Journey Type) ---');
        await page.waitForSelector('.journey-card', { timeout: 5000 });
        const journeyCards = await page.$$('.journey-card');
        await journeyCards[0].click(); // STUDENT
        await new Promise(r => setTimeout(r, 500));
        
        await page.click('button.continue-btn');
        await new Promise(r => setTimeout(r, 1000));
        
        console.log('\n--- Step 5: Onboarding Step 2 - Career Info ---');
        await page.waitForSelector('input[placeholder="Java Full Stack Developer"]', { timeout: 5000 });
        await page.type('input[placeholder="Java Full Stack Developer"]', 'React Developer', { delay: 30 });
        
        await page.select('#onboarding-experience-level', 'BEGINNER');
        await new Promise(r => setTimeout(r, 500));
        
        await page.click('button.continue-btn');
        await new Promise(r => setTimeout(r, 1000));
        
        console.log('\n--- Step 6: Onboarding Step 3 - Career Goal ---');
        await page.waitForSelector('#onboarding-career-goal', { timeout: 5000 });
        await page.select('#onboarding-career-goal', 'JOB');
        await new Promise(r => setTimeout(r, 500));
        
        // Before clicking Complete Profile, dump the token that will be sent
        const tokenBeforeSubmit = await page.evaluate(() => {
            const raw = localStorage.getItem('token');
            if (!raw || raw === "null" || raw === "undefined" || !raw.trim()) return null;
            let cleaned = raw.replace(/^"|"$/g, "").trim();
            if (cleaned.toLowerCase().startsWith("bearer ")) cleaned = cleaned.substring(7).trim();
            return cleaned;
        });
        console.log('  Token that will be sent with PUT:', tokenBeforeSubmit ? `${tokenBeforeSubmit.substring(0, 30)}...(len=${tokenBeforeSubmit.length})` : 'NULL');
        
        console.log('\n--- Step 7: Click "Complete Profile" ---');
        await page.click('button.continue-btn');
        
        // Wait for the PUT /api/profile response
        await new Promise(r => setTimeout(r, 5000));
        
        const urlAfterSubmit = page.url();
        console.log('\n  URL after Complete Profile:', urlAfterSubmit);
        
        // Check for error message
        const obError = await page.evaluate(() => {
            const el = document.querySelector('.onboarding-error-content p');
            return el ? el.textContent : null;
        });
        if (obError) {
            console.log('  ONBOARDING ERROR:', obError);
        }
        
        // Dump final localStorage state
        const finalToken = await page.evaluate(() => localStorage.getItem('token'));
        const finalUser = await page.evaluate(() => localStorage.getItem('user'));
        const finalOb = await page.evaluate(() => localStorage.getItem('onboardingCompleted'));
        console.log('  Final localStorage.token:', finalToken ? `${finalToken.substring(0, 30)}...(len=${finalToken.length})` : 'NULL');
        console.log('  Final localStorage.user:', finalUser ? finalUser.substring(0, 100) : 'NULL');
        console.log('  Final localStorage.onboardingCompleted:', finalOb);
        
        // Take a final screenshot
        await page.screenshot({ path: `C:\\Users\\HP\\.gemini\\antigravity\\brain\\6ea10720-0c8c-4923-bf6a-0b2138f5cf94\\scratch\\final_browser_state.png` });
        console.log('  Final screenshot saved.');
        
        if (urlAfterSubmit.includes('/dashboard')) {
            console.log('\n  *** SUCCESS: Reached Dashboard! ***');
        } else if (urlAfterSubmit.includes('/login') || urlAfterSubmit.includes('/auth')) {
            console.log('\n  *** FAILURE: Redirected back to login/auth ***');
        }
        
        // Print network log summary
        console.log('\n=== NETWORK LOG (backend requests only) ===');
        for (const entry of networkLog) {
            console.log(`  ${entry.method} ${entry.url}`);
            if (entry.headers.authorization) {
                console.log(`    Auth: ${entry.headers.authorization.substring(0, 50)}...`);
            }
        }
        
    } catch (err) {
        console.error('Test error:', err.message);
        await page.screenshot({ path: `C:\\Users\\HP\\.gemini\\antigravity\\brain\\6ea10720-0c8c-4923-bf6a-0b2138f5cf94\\scratch\\error_screenshot.png` });
    }
    
    // Keep browser open for 5 seconds to inspect
    await new Promise(r => setTimeout(r, 5000));
    await browser.close();
    
    console.log('\n=== TEST COMPLETE ===');
})();
