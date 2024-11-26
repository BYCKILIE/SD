import './DeviceCreate.css'
import { useState } from 'react';
import useDevices from "../../hooks/useDevices.js";

const DeviceCreate = () => {
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    // Set up state for the form fields with initial user data.

    const [newData, setNewData] = useState({
        name: '',
        description: '',
        address: '',
        energyConsumption: ''
    });

    const { createDevice } = useDevices();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setNewData((prevData) => ({
            ...prevData,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        setLoading(true);
        e.preventDefault();
        const handle = async () => {
            setError('');
            try {
                const finalEnergy = newData.energyConsumption === '' ? 0 : Number(newData.energyConsumption)
                await createDevice(newData.name, newData.description, newData.address, finalEnergy);

                setNewData({
                    ...newData,
                    name: '',
                    description: '',
                    address: '',
                    energyConsumption: '',
                });
            } catch (err) {
                setError(err.message);
            }
        }

        handle();
        setLoading(false)
    };

    return (
        <div className="wrapper">
            <form onSubmit={handleSubmit}>
                <h1>Device Profile</h1>

                <div className="input-box">
                    <label htmlFor="name">Name</label>
                    <input
                        type="name"
                        name="name"
                        id="name"
                        value={newData.name}
                        onChange={handleChange}
                    />
                </div>

                <div className="input-box">
                    <label htmlFor="description">Description</label>
                    <input
                        type="name"
                        name="description"
                        id="description"
                        value={newData.description}
                        onChange={handleChange}
                    />
                </div>

                <div className="input-box">
                    <label htmlFor="address">Address</label>
                    <input
                        type="name"
                        name="address"
                        id="address"
                        value={newData.address}
                        onChange={handleChange}
                    />
                </div>

                <div className="input-box">
                    <label htmlFor="energyConsumption">Energy Consumption</label>
                    <input
                        type="name"
                        name="energyConsumption"
                        id="energyConsumption"
                        value={newData.energyConsumption}
                        onChange={handleChange}
                    />
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? 'Saving...' : 'Save Changes'}</button>
            </form>
        </div>
    );
};

export default DeviceCreate;

