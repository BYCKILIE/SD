import {createContext, useState} from 'react';
import apiRequest from "../api/axiosConfig.js";

const OwnershipsContext = createContext(undefined);

// eslint-disable-next-line react/prop-types
export const OwnershipsProvider = ({children}) => {
    const [loading, setLoading] = useState(false);

    const map = async (email, deviceId) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/map',
                {
                    email: email,
                    deviceId: deviceId
                },
            );
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    const unmap = async (ownershipId) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/unmap',
                {
                    ownershipId: ownershipId
                },
            );
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    return (
        <OwnershipsContext.Provider value={{
            map,
            unmap
        }}>
            {children}
        </OwnershipsContext.Provider>
    );
};

export default OwnershipsContext;
