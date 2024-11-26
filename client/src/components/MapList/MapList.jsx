import React, { useState } from "react";
import "./MapList.css";
import useDevices from "../../hooks/useDevices.js";
import {useQuery} from "../../hooks/useQuery.js";
import useOwnerships from "../../hooks/useOwnerships.js";

const MapList = () => {
    const [searchQuery, setSearchQuery] = useState("");
    const [xmlResp, setXmlResp] = useState([]);

    const {fetchAvailableDevices} = useDevices();

    const offset = () => {
        if (searchQuery === "" || searchQuery.length < 2) {
            return 0;
        }
        return -1;
    }

    const getDevices = async () => {
        try {

            const devices = await fetchAvailableDevices(searchQuery, offset());
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
            setXmlResp(null);
        }
    };

    useState(() => {
        const handleDevices = async () => {
            await getDevices();
        }

        handleDevices();
    })

    const handleSearchChange = (event) => {
        setSearchQuery(event.target.value.toLowerCase());
        getDevices();
    };

    return (
        <div className="device-list-container">
            <input
                type="text"
                placeholder="Search by name..."
                className="search-bar"
                value={searchQuery}
                onChange={handleSearchChange}
            />

            <div className="users">
                {xmlResp.length > 0 ? (
                    xmlResp.map((device, index) => (
                        <Map
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
                    <p>No users found.</p>
                )}
            </div>
        </div>
    );
};

// eslint-disable-next-line react/prop-types
const Map = ({ id, name, description, address, energyConsumption }) => {
    const query = useQuery();
    const email = query.get("email");

    const { map } = useOwnerships()

    const onSubmit = () => {
        map(email, id)
    }

    return (
        <div className={`device-container`}>
            <div className="device-info">
                <p><strong>Device:</strong>{id}</p>
                <p><strong>Name:</strong> {name}</p>
                <p><strong>Description:</strong> {description}</p>
                <p><strong>Address:</strong> {address}</p>
                <p><strong>Energy Consumption:</strong> {energyConsumption} kWh</p>
            </div>
            <div className="conf">
                <button className="map-button" onClick={onSubmit}>Map</button>
            </div>
        </div>
    );
};

export default MapList;
