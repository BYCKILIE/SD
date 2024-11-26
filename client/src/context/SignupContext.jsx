// eslint-disable-next-line no-unused-vars
import React, { createContext, useState } from 'react';
import apiRequest from "../api/axiosConfig.js";

const SignupContext = createContext(undefined);

// eslint-disable-next-line react/prop-types
export const SignupProvider = ({ children }) => {
    const [loading, setLoading] = useState(false);

    const signup = async (userData) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/signup', userData);

            const status = response.status;
            console.log('Response Status:', status);

            if (status === 201) {
                console.log('Signup successful:', response.data);
            } else {
                console.log('Signup failed with status:', status);
            }

        } catch (error) {
            console.error('Signup error:', error);
            throw new Error('Signup failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <SignupContext.Provider value={{ signup, loading }}>
            {children}
        </SignupContext.Provider>
    );
};

export default SignupContext;
