const userRolesUrl = API_CONFIG.API_BASE_URL + '/user/roles';

document.addEventListener('DOMContentLoaded', async function() {
    try {
        await checkUserSessionForPermissions();
    
    } catch (error) {
        console.error('Page initialization failed:', error);
    }
});

// Checks like this are why this page should be served from the server or be an SPA.
async function checkUserSessionForPermissions() {
    const response = await fetch(userRolesUrl, {
        method: 'GET',
        credentials: 'include'
    });

    if (response.status === 403) {
        window.location = 'login.html';
        return;
    }

    if (!response.ok) {
        throw new Error(`Failed to fetch roles: ${response.status}`);
    }

    const roles = await response.json();
    roles.forEach(role => {
        if (role === 'ROLE_ADMIN') {
            document.getElementById('admin-link').removeAttribute('hidden');
            document.getElementById('table-admin-link').removeAttribute('hidden');
            isAdmin = true;
        }
    });
}

