import "../styles/home.css";
import InfoType from "../components/InfoType/InfoType.jsx";


// eslint-disable-next-line react/prop-types
const MenuForm = ({ names, headers, activeTab, changeTabOnClick, greeting="Welcome", mode }) => {
    return (
        <div className="tabs-body">
            {/* eslint-disable-next-line react/prop-types */}
            <h1 className="title"><span>[{greeting}]</span> {names.firstName} {names.lastName}</h1>
            <TabHeader data={headers} activeId={activeTab} click={changeTabOnClick}/>
            <TabContent activeId={activeTab} mode={mode}/>
        </div>
    );
};

// eslint-disable-next-line react/prop-types
const TabHeader = ({data, activeId, click}) => {
    return (
        <ul className="tabs-header">
            {/* eslint-disable-next-line react/prop-types */}
            {data.map((item, index) => (
                <li key={index} className={activeId === index ? 'active' : ''}>
                    <a onClick={() => click(index)}>
                        <span>{item}</span>
                    </a>
                </li>
            ))}
        </ul>
    );
};

// eslint-disable-next-line react/prop-types
const TabContent = ({activeId, mode}) => {
    return (
        <div className="tabs-content">
            <div className="tabs-textItem show">
                <InfoType index={activeId} mode={mode}/>
            </div>
        </div>
    );
};

export default MenuForm;
