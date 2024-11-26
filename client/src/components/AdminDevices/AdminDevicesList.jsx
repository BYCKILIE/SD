import React, { useState } from "react";
import "./AdminDevicesList.css";
import {useNavigate} from "react-router-dom";
import useDevices from "../../hooks/useDevices.js";

const AdminDevicesList = () => {
    const [expandedUser, setExpandedUser] = useState(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [xmlResp, setXmlResp] = useState([]);

    const {fetchAvailableDevices} = useDevices();

    const offset = () => {
        if (searchQuery === "" || searchQuery.length < 2) {
            return 0;
        }
        setExpandedUser(null);
        return -1;
    }

    const getUsers = async () => {
        try {
            const users = await fetchAvailableDevices(searchQuery, offset());
            if (!users) {
                setXmlResp([]);
            } else if (Array.isArray(users)) {
                setXmlResp(users);
            } else {
                setXmlResp([users]);
            }
        } catch {
            setXmlResp([]);
        }
    };

    useState(() => {
        const handleUsers = async () => {
            await getUsers();
        }

        handleUsers();
    })

    const handleExpandClick = (deviceId) => {
        setExpandedUser(expandedUser === deviceId ? null : deviceId);
    };

    const handleSearchChange = (event) => {
        setSearchQuery(event.target.value.toLowerCase());
        getUsers();
    };

    return (
        <div className="users-list-container">
            <input
                type="text"
                placeholder="Search by name..."
                className="search-bar"
                value={searchQuery}
                onChange={handleSearchChange}
            />

            <div className="users">
                {xmlResp && xmlResp.length > 0 ? (
                    xmlResp.map((device, index) => (
                        <User
                            key={index}
                            deviceId={device.deviceId}
                            name={device.name}
                            description={device.description}
                            address={device.address}
                            energyConsumption={device.energyConsumption}
                            isExpanded={expandedUser === device.deviceId}
                            onClick={() => handleExpandClick(device.deviceId)}
                        />
                    ))
                ) : (
                    <p>No users found.</p>
                )}
            </div>
        </div>
    );
};

// eslint-disable-next-line react/prop-types
const User = ({ deviceId, name, description, address, energyConsumption, isExpanded, onClick }) => {
    const navigate = useNavigate();

    const { deleteDevice } = useDevices();

    const onSubmit = () => {
        navigate("/admin/device?deviceId=" + deviceId);
    }

    const onDelete = () => {
        deleteDevice(deviceId);
    }

    return (
        <div className={`user-container ${isExpanded ? "expanded" : ""}`} onClick={onClick}>
            <div className="user-info">
                <p><strong>{name}</strong></p>
                <p><strong>Device Id:</strong> {deviceId}</p>
                <p><strong>Description:</strong> {description}</p>
                <p><strong>Address:</strong> {address}</p>
                <p><strong>Energy Consumption:</strong> {energyConsumption} kWh</p>
            </div>
            {isExpanded && (
                <div className="expanded-content">
                    <button className="details-button" onClick={onSubmit}>Edit</button>
                    <button className="delete-button" onClick={onDelete}>Delete</button>
                </div>
            )}
        </div>
    );
};

export default AdminDevicesList;
