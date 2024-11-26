import axios from 'axios';

const apiRequest = (api) => {
    const instance = axios.create({
        baseURL: `https://sd.${api}.bchportal.net`,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
    });

    instance.interceptors.request.use(
        (config) => {
            const localToken = localStorage.getItem('Token');
            const sessionToken = sessionStorage.getItem('Token');

            const token = localToken || sessionToken;
            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`
            }

            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );

    instance.interceptors.response.use(
        (response) => {
            return response;
        },
        (error) => {

            if (error.response) {
                if (error.response.status === 401) {
                    console.error('Unauthorized! Redirecting to login...');
                }
            } else {
                console.error('Error occurred: No response from the server.', error.message);
            }
            return Promise.reject(error);
        }
    );

    return instance;
};

export default apiRequest;
