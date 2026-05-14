const API_BASE = '/api';

const app = {
    state: {
        voter: null,
        isAdmin: false
    },

    // UI Navigation
    showSection: (sectionId) => {
        document.querySelectorAll('section').forEach(s => s.classList.add('hidden'));
        document.getElementById(sectionId).classList.remove('hidden');
    },

    logout: () => {
        app.state.voter = null;
        app.state.isAdmin = false;
        document.getElementById('main-nav').classList.remove('hidden');
        document.getElementById('logout-btn').classList.add('hidden');
        app.showSection('login-section');
    },

    // API Helpers
    fetchData: async (endpoint, method = 'GET', data = null) => {
        const options = {
            method,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        };
        if (data) {
            options.body = new URLSearchParams(data).toString();
        }
        try {
            const res = await fetch(`${API_BASE}${endpoint}`, options);
            const text = await res.text();
            try {
                return JSON.parse(text); // Try parse JSON
            } catch {
                return { message: text, status: res.status };
            }
        } catch (err) {
            console.error("API Error", err);
            return { error: true, message: 'Network error.' };
        }
    },

    // Handlers
    init: () => {
        // Voter Login
        document.getElementById('login-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('login-id').value;
            const pass = document.getElementById('login-pass').value;
            
            const res = await app.fetchData('/login', 'POST', { id, password: pass });
            if (res.error) {
                document.getElementById('login-error').innerText = res.message;
            } else {
                app.state.voter = res.voter;
                app.onLoginSuccess();
            }
        });

        // Voter Registration
        document.getElementById('register-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                id: document.getElementById('reg-id').value,
                name: document.getElementById('reg-name').value,
                age: document.getElementById('reg-age').value,
                constituency: document.getElementById('reg-constituency').value,
                password: document.getElementById('reg-pass').value
            };
            const res = await app.fetchData('/register', 'POST', data);
            document.getElementById('reg-msg').innerText = res.message;
            if (!res.error) {
                setTimeout(() => app.showSection('login-section'), 2000);
            }
        });

        // Admin Login
        document.getElementById('admin-login-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const user = document.getElementById('admin-user').value;
            const pass = document.getElementById('admin-pass').value;
            const res = await app.fetchData('/admin/login', 'POST', { username: user, password: pass });
            
            if (res.error) {
                document.getElementById('admin-login-error').innerText = res.message;
            } else {
                app.state.isAdmin = true;
                app.onAdminLoginSuccess();
            }
        });

        // Add Candidate
        document.getElementById('add-candidate-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                id: document.getElementById('cand-id').value,
                name: document.getElementById('cand-name').value,
                party: document.getElementById('cand-party').value,
                constituency: document.getElementById('cand-constituency').value,
                manifesto: document.getElementById('cand-manifesto').value
            };
            const res = await app.fetchData('/admin/add-candidate', 'POST', data);
            document.getElementById('admin-msg').innerText = res.message;
            e.target.reset();
        });
    },

    onLoginSuccess: async () => {
        document.getElementById('main-nav').classList.add('hidden');
        document.getElementById('logout-btn').classList.remove('hidden');
        document.getElementById('voter-welcome').innerText = `Welcome, ${app.state.voter.name}`;
        app.showSection('voter-dashboard-section');

        if (app.state.voter.hasVoted) {
            document.getElementById('voting-area').innerHTML = `<h3>You have already securely cast your encrypted vote. Thank you!</h3>`;
        } else {
            app.loadCandidates();
        }
    },

    loadCandidates: async () => {
        const res = await app.fetchData(`/candidates?constituency=${app.state.voter.constituency}`);
        const grid = document.getElementById('candidates-grid');
        grid.innerHTML = '';
        
        if (res.candidates && res.candidates.length > 0) {
            res.candidates.forEach(c => {
                const div = document.createElement('div');
                div.className = 'candidate-card';
                div.innerHTML = `
                    <h4>${c.name}</h4>
                    <p>${c.party}</p>
                    <button class="primary-btn" onclick="app.castVote('${c.id}')">Vote</button>
                `;
                grid.appendChild(div);
            });
        } else {
            grid.innerHTML = '<p>No candidates available in your constituency.</p>';
        }
    },

    castVote: async (candidateId) => {
        const res = await app.fetchData('/vote', 'POST', { 
            voterId: app.state.voter.id, 
            candidateId 
        });
        
        if (res.error) {
            alert(res.message);
        } else {
            document.getElementById('voting-area').classList.add('hidden');
            document.getElementById('feedback-area').classList.remove('hidden');
        }
    },


    submitFeedback: async () => {
        const text = document.getElementById('feedback-input').value;
        const res = await app.fetchData('/ai/sentiment', 'POST', { text });
        document.getElementById('sentiment-result').innerText = `AI Tagged you as: ${res.sentiment}`;
    },

    // Admin Functions
    onAdminLoginSuccess: () => {
        document.getElementById('main-nav').classList.add('hidden');
        document.getElementById('logout-btn').classList.remove('hidden');
        app.showSection('admin-dashboard-section');
        app.checkElectionStatus();
    },

    checkElectionStatus: async () => {
        const res = await app.fetchData('/admin/status');
        document.getElementById('election-status-display').innerText = `Election is currently: ${res.status}`;
    },

    toggleElection: async () => {
        await app.fetchData('/admin/toggle-status', 'POST');
        app.checkElectionStatus();
    },

    generateResults: async () => {
        const res = await app.fetchData('/admin/results');
        document.getElementById('results-display').innerText = res.report || res.message;
    },

};

window.onload = app.init;
