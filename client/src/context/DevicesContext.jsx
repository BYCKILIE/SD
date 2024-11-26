import {createContext, useState} from 'react';
import apiRequest from "../api/axiosConfig.js";
import {XMLParser} from 'fast-xml-parser';

const DeviceContext = createContext(undefined);

// eslint-disable-next-line react/prop-types
export const DevicesProvider = ({children}) => {
    const [loading, setLoading] = useState(false);

    const fetchUserDevices = async (currOffset) => {

        setLoading(true);
        try {
            const response = await apiRequest("devices").post(
                '/user/fetch',
                {offset: currOffset.toString()}
            );

            if (response.status === 200) {
                const parser = new XMLParser();
                const parsedResult = parser.parse(response.data);

                return parsedResult.devices.device;
            } else {
                return null
            }
        } finally {
            setLoading(false);
        }
    };

    const fetchAvailableDevices = async (name, currOffset) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/available',
                {partialName: name, offset: currOffset.toString()}
            );
            if (response.status === 200) {
                const parser = new XMLParser();
                const parsedResult = parser.parse(response.data);

                return parsedResult.devices.device;
            } else {
                return null
            }
        } finally {
            setLoading(false)
        }
    }

    const fetchAdminDevices = async (email, currOffset) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/fetch',
                {email: email, offset: currOffset.toString()}
            );
            if (response.status === 200) {
                const parser = new XMLParser();
                const parsedResult = parser.parse(response.data);

                return parsedResult.devices.device;
            } else {
                return null
            }
        }
        finally {
            setLoading(false)
        }
    }

    const createDevice = async (name, description, address, energyConsumption) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/create',
                {
                    name: name,
                    description: description,
                    address: address,
                    energyConsumption: energyConsumption.toString()
                },
            );
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    const readDevice = async (deviceId) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/read',
                {
                    deviceId: deviceId
                },
            );
            if (response.status === 200) {
                return response.data
            } else {
                return null
            }
        } finally {
            setLoading(false)
        }
    }

    const updateDevice = async (deviceId, name, description, address, energyConsumption) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/update',
                {
                    id: deviceId,
                    name: name,
                    description: description,
                    address: address,
                    energyConsumption: energyConsumption
                },
            );
            return response.status === 200
        }
        catch (e) {
            console.log(e)
        }
        finally {
            setLoading(false)
        }
    }

    const deleteDevice = async (deviceId) => {
        setLoading(true);
        try {
            const response = await apiRequest("devices").post('/admin/delete',
                {
                    deviceId: deviceId,
                },
            );
            return response.status === 200
        } finally {
            setLoading(false)
        }
    }

    return (
        <DeviceContext.Provider value={{
            fetchUserDevices,
            fetchAvailableDevices,
            fetchAdminDevices,
            createDevice,
            readDevice,
            updateDevice,
            deleteDevice
        }}>
            {children}
        </DeviceContext.Provider>
    );
};

export default DeviceContext;
