import {createContext, useState} from 'react';
import apiRequest from "../api/axiosConfig.js";

const AuthContext = createContext(undefined);

// eslint-disable-next-line react/prop-types
export const AuthProvider = ({children}) => {
    const [loading, setLoading] = useState(false);
    const [client, setClient] = useState(null);
    const [admin, setAdmin] = useState(null);

    const getToken = () => {
        return localStorage.getItem('Token') || sessionStorage.getItem('Token');
    };

    const clientAuth = async () => {
        if (!getToken()) {
            console.error('No token found');
            return;
        }

        setLoading(true);
        try {
            const response = await apiRequest("users").get('/auth');

            // Check if response is valid and contains necessary data
            if (response.status === 200 && response.data) {
                console.log('Client authentication response:', response.data);

                // Validate the role in the response data
                if (response.data.role === "client") {
                    setClient(response.data); // Correctly set the client state
                } else {
                    console.error('Role mismatch or not a client');
                    setClient(null); // Reset client if role is not as expected
                }
            } else {
                console.error('Unexpected response status or empty data', response);
                setClient(null);
            }
        } catch (error) {
            console.error('Token verification error:', error);
            setClient(null); // Reset client on error
        } finally {
            setLoading(false);
        }
    };

    const adminAuth = async () => {
        if (!getToken()) return;
        setLoading(true);
        try {
            const response = await apiRequest("users").get('/admin');
            // Check if response is valid and contains necessary data
            if (response.status === 200 && response.data) {
                // Validate the role in the response data
                if (response.data.role === "admin") {
                    setAdmin(response.data); // Correctly set the client state
                } else {
                    console.error('Role mismatch or not a client');
                    setAdmin(null); // Reset client if role is not as expected
                }
            } else {
                console.error('Unexpected response status or empty data', response);
                setAdmin(null);
            }
        } catch (error) {
            console.error('Admin token verification error:', error);
        } finally {
            setLoading(false);
        }
    };

    const login = async (credentials, rememberMe) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/login', credentials);

            if (response.status === 200) {
                const userData = response.data;
                const token = userData.token;

                if (rememberMe) {
                    localStorage.setItem('Token', token);
                } else {
                    sessionStorage.setItem('Token', token);
                }

                if (userData.role === "client") {
                    setClient(response.data);
                } else if (userData.role === "admin") {
                    setAdmin(response.data);
                }
            }
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        } finally {
            setLoading(false);
        }
    };

    const logout = () => {
        localStorage.removeItem('Token');
        sessionStorage.removeItem('Token');
        setClient(null);
        setAdmin(null);
    };

    return (
        <AuthContext.Provider value={{client, admin, clientAuth, adminAuth, login, logout, loading}}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthContext;
