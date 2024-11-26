import './Devices.css';
import { useEffect, useState } from "react";
import useDevices from "../../hooks/useDevices.js";

const DevicesList = () => {
    const [xmlResp, setXmlResp] = useState([]);

    const {fetchUserDevices} = useDevices();

    const getDevices = async () => {
        try {
            const devices = await fetchUserDevices(0);

            if (Array.isArray(devices)) {
                setXmlResp(devices);
            } else {
                setXmlResp([devices]);
            }
        } catch {
            setXmlResp(null)
        }
    };

    useEffect(() => {
        const handle = async () => {
            await getDevices();
        }
        handle();
    })

    return (
        <div className="devices">
            {/* eslint-disable-next-line react/prop-types */}
            {xmlResp && xmlResp.length > 0 ? (
                // eslint-disable-next-line react/prop-types
                xmlResp.map((device, index) => (
                    <Device
                        key={index}
                        className={"device"}
                        id={device.deviceId}
                        name={device.name}
                        description={device.description}
                        address={device.address}
                        energyConsumption={device.energyConsumption}
                    />
                ))
            ) : (
                <p>No devices found.</p> // Show a fallback if there are no devices
            )}
        </div>
    );
};

// eslint-disable-next-line react/prop-types
const Device = ({ id, name, description, address, energyConsumption }) => {
    return (
        <div className="device-container">
            <p><strong>Device:</strong>{id}</p>
            <p><strong>Name:</strong> {name}</p>
            <p><strong>Description:</strong> {description}</p>
            <p><strong>Address:</strong> {address}</p>
            <p><strong>Energy Consumption:</strong> {energyConsumption} kWh</p>
        </div>
    );
};

export default DevicesList;
