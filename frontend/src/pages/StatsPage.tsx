import React, { useEffect, useState } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Scatter } from 'react-chartjs-2';
import { buttonApi } from '../services/api';
import { StatsResponse, ButtonPressData } from '../types/Button';
import Header from '../components/Header';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const StatsPage: React.FC = () => {
  const [stats, setStats] = useState<StatsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);


  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        // Calculate timestamps for last 30 days
        const endDate = new Date();
        const startDate = new Date();
        startDate.setDate(endDate.getDate() - 30);
        
        const startTimestamp = startDate.toISOString();
        const endTimestamp = endDate.toISOString();
        
        const statsData = await buttonApi.getStats(startTimestamp, endTimestamp);
        setStats(statsData);
      } catch (err) {
        setError('Failed to load statistics');
        console.error('Error fetching stats:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const getDateIndex = (dateStr: string, startDate: Date): number => {
    const date = new Date(dateStr);
    const diffTime = date.getTime() - startDate.getTime();
    return Math.floor(diffTime / (1000 * 60 * 60 * 24));
  };

  const prepareChartData = () => {
    if (!stats) return { datasets: [] };

    // Calculate the date range (last 30 days)
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - 29);

    const datasets = stats.buttonStats.map((buttonStat, index) => ({
      label: buttonStat.buttonTitle,
      data: buttonStat.presses.map((press: ButtonPressData) => ({
        x: getDateIndex(press.date, startDate),
        y: press.hour,
      })),
      backgroundColor: buttonStat.buttonColor,
      borderColor: buttonStat.buttonColor,
      pointRadius: 4,
      pointHoverRadius: 6,
    }));

    return { datasets };
  };

  const getDateLabels = () => {
    const labels = [];
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - 29);

    for (let i = 0; i < 30; i++) {
      const date = new Date(startDate);
      date.setDate(startDate.getDate() + i);
      labels.push(date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }));
    }
    return labels;
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          usePointStyle: true,
          padding: 20,
        },
      },
      title: {
        display: true,
        text: 'Button Press Activity - Last 30 Days',
        font: {
          size: 16,
        },
      },
      tooltip: {
        callbacks: {
          title: (context: any) => {
            const dateIndex = context[0].parsed.x;
            const endDate = new Date();
            const startDate = new Date();
            startDate.setDate(endDate.getDate() - 29);
            const date = new Date(startDate);
            date.setDate(startDate.getDate() + dateIndex);
            return date.toLocaleDateString();
          },
          label: (context: any) => {
            const hour = context.parsed.y;
            const buttonTitle = context.dataset.label;
            const time = hour === 0 ? '12:00 AM' : 
                        hour < 12 ? `${hour}:00 AM` : 
                        hour === 12 ? '12:00 PM' : 
                        `${hour - 12}:00 PM`;
            return `${buttonTitle} at ${time}`;
          },
        },
      },
    },
    scales: {
      x: {
        type: 'linear' as const,
        position: 'bottom' as const,
        title: {
          display: true,
          text: 'Date',
        },
        ticks: {
          callback: function(value: any) {
            const labels = getDateLabels();
            return labels[Math.floor(value)] || '';
          },
          stepSize: 1,
        },
        min: 0,
        max: 29,
      },
      y: {
        title: {
          display: true,
          text: 'Hour of Day',
        },
        ticks: {
          stepSize: 1,
          callback: function(value: any) {
            const hour = Math.floor(value);
            if (hour === 0) return '12 AM';
            if (hour < 12) return `${hour} AM`;
            if (hour === 12) return '12 PM';
            return `${hour - 12} PM`;
          },
        },
        min: 0,
        max: 23,
      },
    },
  };

  if (loading) {
    return (
      <div className="container-fluid py-4">
        <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading statistics...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container-fluid py-4">
        <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
          <div className="alert alert-danger" role="alert">
            {error}
          </div>
        </div>
      </div>
    );
  }

  const chartData = prepareChartData();
  const hasData = stats && stats.buttonStats.some(buttonStat => buttonStat.presses.length > 0);

  return (
    <>
      <Header />
      <div className="w-100 p-3">
        <h1 className="h3 mb-4">Button Press Statistics</h1>
        
        {!hasData ? (
          <div className="text-center py-5">
            <p className="fs-5 mb-3 text-muted">No button presses found</p>
            <p className="text-muted">Press some buttons to see your activity graph!</p>
          </div>
        ) : (
          <>
            <div style={{ 
              height: isMobile ? '500px' : '600px', 
              width: '100%',
              marginBottom: '2rem' 
            }}>
              <Scatter data={chartData} options={chartOptions} />
            </div>
            
            {stats && (
              <div className="mt-4">
                <h5 className="mb-3">Legend</h5>
                <div className="row">
                  {stats.buttonStats.map((buttonStat) => (
                    <div key={buttonStat.buttonId} className="col-md-6 col-lg-4 mb-2">
                      <div className="d-flex align-items-center">
                        <div
                          className="me-2"
                          style={{
                            width: '16px',
                            height: '16px',
                            borderRadius: '50%',
                            backgroundColor: buttonStat.buttonColor,
                          }}
                        ></div>
                        <span className="text-truncate">
                          {buttonStat.buttonTitle} ({buttonStat.presses.length} presses)
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </>
  );
};

export default StatsPage;