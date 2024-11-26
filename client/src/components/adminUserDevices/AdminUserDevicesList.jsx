import './AdminUserDevices.css';
import React, { useEffect, useState } from "react";
import useDevices from "../../hooks/useDevices.js";
import {useQuery} from "../../hooks/useQuery.js";
import useOwnerships from "../../hooks/useOwnerships.js";

const AdminUserDevicesList = () => {
    const query = useQuery();
    const email = query.get("email")

    const [xmlResp, setXmlResp] = useState([]);

    const {fetchAdminDevices} = useDevices();

    const getDevices = async () => {
        try {
            const devices = await fetchAdminDevices(email, 0);

            if (!devices) {
                setXmlResp([])
                return;
            }
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
            {xmlResp && xmlResp.length > 0 ? (
                xmlResp.map((device, index) => (
                    <AdminUserDevice
                        key={index}
                        className={"device"}
                        ownership={device.ownershipId}
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
const AdminUserDevice = ({ ownership, id, name, description, address, energyConsumption }) => {
    const {unmap} = useOwnerships();

    const onUnmap = () => {
        unmap(ownership);
    }

    return (
        <div className="device-container">
            <div className="device-info">
                <p><strong>Ownership:</strong>{ownership}</p>
                <p><strong>Device:</strong>{id}</p>
                <p><strong>Name:</strong> {name}</p>
                <p><strong>Description:</strong> {description}</p>
                <p><strong>Address:</strong> {address}</p>
                <p><strong>Energy Consumption:</strong> {energyConsumption} kWh</p>
            </div>
            <div className="conf">
                <button className="unmap-button" onClick={onUnmap}>Unmap</button>
            </div>
        </div>
    );
};

export default AdminUserDevicesList;
