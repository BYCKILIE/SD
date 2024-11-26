import {createContext, useState} from 'react';
import apiRequest from "../api/axiosConfig.js";
import {XMLParser} from 'fast-xml-parser';

const UsersContext = createContext(undefined);

// eslint-disable-next-line react/prop-types
export const UsersProvider = ({children}) => {
    const [loading, setLoading] = useState(false);

    const fetchUsers = async (partialName, currOffset) => {

        setLoading(true);
        try {
            const response = await apiRequest("users").post(
                '/fetch',
                { name: partialName, offset: currOffset.toString() }
            );

            if (response.status === 200) {
                const parser = new XMLParser();
                const parsedResult = parser.parse(response.data);

                return parsedResult.profiles.data;
            } else {
                return null
            }
        } catch (e) {
            console.error(e);
        }
        finally {
            setLoading(false);
        }
    };

    const readUser = async () => {
        setLoading(true);
        try {
            const response = await apiRequest("users").get('/user/read');
            if (response.status === 200) {
                return response.data
            } else {
                return null
            }
        } finally {
            setLoading(false)
        }
    }

    const updateUser = async (email, password) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/user/update',
                {
                    newEmail: email,
                    newPassword: password,
                },
            );
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    const deleteUser = async () => {
        setLoading(true);
        try {
            const response = await apiRequest("users").get('/user/delete');
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    const promoteUser = async (email) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/admin-user/role', {
                email: email
            });
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    const readAdminUser = async (email) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/admin-user/read', {
                wantedEmail: email
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

    const updateAdminUser = async (email, newEmail, newPassword) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/admin-user/update',
                {
                    email: email,
                    newEmail: newEmail,
                    newPassword: newPassword,
                },
            );
            return response.status === 200
        } catch (e) {
            console.error(e);
        }
        finally {
            setLoading(false)
        }
    }

    const deleteAdminUser = async (email) => {
        setLoading(true);
        try {
            const response = await apiRequest("users").post('/admin-user/delete', {
                email: email
            });
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    return (
        <UsersContext.Provider value={{
            fetchUsers,
            readUser,
            updateUser,
            deleteUser,
            promoteUser,
            readAdminUser,
            updateAdminUser,
            deleteAdminUser
        }}>
            {children}
        </UsersContext.Provider>
    );
};

export default UsersContext;
