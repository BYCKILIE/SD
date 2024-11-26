import './DeviceEdit.css'
import {useState, useEffect} from 'react';
import useDevices from "../../hooks/useDevices.js";
import {useQuery} from "../../hooks/useQuery.js";

const DeviceEdit = () => {
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    // Set up state for the form fields with initial user data.
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        address: '',
        energyConsumption: ''
    });

    const [newData, setNewData] = useState({
        name: '',
        description: '',
        address: '',
        energyConsumption: ''
    });

    const query = useQuery();
    const deviceId = query.get("deviceId");
    const {readDevice, updateDevice} = useDevices();

    useEffect(() => {
        const getData = async () => {
            const device = await readDevice(deviceId)

            setFormData({
                ...formData,
                name: device.name,
                description: device.description,
                address: device.address,
                energyConsumption: device.energyConsumption,
            });
        }
        getData()
    })

    const handleChange = (e) => {
        const {name, value} = e.target;
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
                await updateDevice(deviceId, newData.name, newData.description, newData.address, finalEnergy);
                window.location.reload();
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
                        placeholder={formData.name}
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
                        placeholder={formData.description}
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
                        placeholder={formData.address}
                        value={newData.address}
                        onChange={handleChange}
                    />
                </div>

                <div className="input-box">
                    <label htmlFor="energyConsumption">Energy Consumption</label>
                    <input
                        type="numeric"
                        name="energyConsumption"
                        id="energyConsumption"
                        placeholder={formData.energyConsumption}
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

export default DeviceEdit;

