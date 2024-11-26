import {createContext, useState} from 'react';
import apiRequest from "../api/axiosConfig.js";

const ProfilesContext = createContext(undefined);

// eslint-disable-next-line react/prop-types
export const ProfilesProvider = ({children}) => {
    const [loading, setLoading] = useState(false);

    const readProfile = async () => {
        setLoading(true);
        try {
            const response = await apiRequest("users").get('/profile/read');
            if (response.status === 200) {
                return response.data
            } else {
                return null
            }
        } finally {
            setLoading(false)
        }
    }

    const updateProfile = async (firstName, lastName) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/profile/update',
                {
                    firstName: firstName,
                    lastName: lastName,
                },
            );
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    const readAdminProfile = async (email) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/admin-profile/read', {
                email: email
            });
            if (response.status === 200) {
                return response.data
            } else {
                return null
            }
        } finally {
            setLoading(false)
        }
    }

    const updateAdminProfile = async (email, firstName, lastName) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/admin-profile/update',
                {
                    email: email,
                    firstName: firstName,
                    lastName: lastName
                },
            );
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    return (
        <ProfilesContext.Provider value={{
            readProfile,
            updateProfile,
            readAdminProfile,
            updateAdminProfile,
        }}>
            {children}
        </ProfilesContext.Provider>
    );
};

export default ProfilesContext;
