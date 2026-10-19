import './App.css'
import * as React from "react";
import {useState} from "react";

let routeId: number = 0;

function App() {
    const stations = ['Alexanderplatz', 'Hermannplatz'];
    const [from, setFrom] = useState('');
    const [to, setTo] = useState('');
    const [time, setTime] = useState('08:30');

    type Commute = { id: number; from: string; to: string; time: string };
    const [commutes, setCommutes] = useState<Commute[]>(
        () => {
            try {
                return JSON.parse(localStorage.getItem('routes') ?? '[]') as Commute[];
            } catch {
                return [];
            }
        }
    );
    const saveRoute = (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!from || !to || from == to) {
            return;
        }

        // TODO: send POST request to backend

        const next = [{id: routeId++, from, to, time}, ...commutes];
        setCommutes(next);

        localStorage.setItem('routes', JSON.stringify(next));
        setFrom('');
        setTo('');
    };

    const deleteRoute = (id: number) => {
        var filtered = commutes.filter((comm) => comm.id !== id);
        setCommutes(filtered);
        localStorage.setItem('routes', JSON.stringify(filtered));
    }

    return (
        <>
            <header className="topbar">
                <h1> Berlin Transit Tracker </h1>
            </header>

            <main className="layout">
                <section className="panel">

                    <h2>Add a commute</h2>

                    <form onSubmit={saveRoute}>
                        <label htmlFor="from">Station A</label>
                        <select id="from" value={from} onChange={event => setFrom(event.target.value)} required>
                            <option value="" disabled>Select a station</option>
                            {stations.map(station => <option key={station}>{station}</option>)}
                        </select>

                        <label htmlFor="to">Station B</label>
                        <select id="to" value={to} onChange={event => setTo(event.target.value)} required>
                            <option value="" disabled>Select a station</option>
                            {stations.map(station => <option key={station}>{station}</option>)}
                        </select>

                        <label htmlFor="time">Departure time</label>
                        <input id="time" type="time" value={time} onChange={event => setTime(event.target.value)}
                               required/>

                        <button type="submit" disabled={!from || !to || from == to}>
                            Save Route
                        </button>
                    </form>
                </section>

                <section className="commutes">
                    <h2>Your commutes</h2>
                    {commutes.length === 0 ? <p className="empty">No saved commutes yet.</p> : commutes.map(commute =>
                        <article className="commute-card" key={commute.id}>
                            <div><strong>{commute.from} → {commute.to}</strong><p>{commute.time}</p></div>
                            <button className="delete-button" onClick={() => deleteRoute(commute.id)}>Delete</button>
                        </article>)}
                </section>
            </main>
        </>
    )
}

export default App
