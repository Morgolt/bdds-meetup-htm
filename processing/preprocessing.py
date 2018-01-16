import os

import numpy as np
import pandas as pd


def get_train_csv(fn, num_trips=10, resample=15, out=None):
    df = pd.read_csv(fn, header=None,
                     names=['name', 'time', 'lon', 'lat', 'altitude', 'speed', 'heading', 'acc', 'nan'])
    df = df.drop(['name', 'altitude', 'heading', 'acc', 'nan'], axis=1)
    df.loc[:, 'dtime'] = pd.to_datetime(df.time, unit='ms')
    df.loc[:, 'trip_id'] = 1
    if resample:
        df = df.resample('{}S'.format(resample), on='dtime').first().reset_index(drop=True)
    final_df = df.copy()
    for i in range(2, num_trips):
        test_df = df.copy()
        test_df.loc[:, 'trip_id'] = i
        test_df.loc[:, 'dtime'] = _change_time(test_df.dtime)
        final_df = final_df.append(test_df)
    if out:
        df_to_csv(final_df, out)
    return final_df


def add_noise(df):
    pass


def lower_diskretization_det(df):
    pass


def lower_diskretization_rand(df):
    pass


def _change_time(time: pd.Series):
    pm = np.random.randint(0, 2)
    if pm == 0:
        newt = time + pd.Timedelta(days=1,
                                   hours=np.random.randint(0, 2),
                                   minutes=np.random.randint(0, 30),
                                   seconds=np.random.randint(0, 60))
    else:
        newt = time - pd.Timedelta(days=1,
                                   hours=np.random.randint(0, 2),
                                   minutes=np.random.randint(0, 30),
                                   seconds=np.random.randint(0, 60))
    return newt


def df_to_csv(df, out):
    df.to_csv(out,
              index=False,
              header=False,
              columns=['trip_id', 'lat', 'lon', 'dtime', 'speed'])


def invert_sequence(df, out):
    seq = df.loc[df.trip_id == 1, :].iloc[::-1].reset_index().copy()
    seq.loc[:, 'dtime'] = _change_time(df.loc[df.trip_id == 1, :].dtime)
    seq.loc[:, 'trip_id'] = df.trip_id.max() + 1
    df = df.append(seq)
    df_to_csv(df, out)


def change_velocity_on_segment(df, out):
    seq = df.loc[df.trip_id == 1, :]
    seq.loc[:, 'dtime'] = _change_time(df.loc[df.trip_id == 1, :].dtime)
    seq.loc[:, 'trip_id'] = df.trip_id.max() + 1
    # todo: extend


if __name__ == '__main__':
    RAW_PATH = 'D:\\Projects\\anomaly-detection-htm\\data'
    TRAIN_PATH = 'D:\\Projects\\anomaly-detection-htm\\data\\train'
    df = get_train_csv(os.path.join(RAW_PATH, '7_kk_st_sec.csv'))
    invert_sequence(df, os.path.join(TRAIN_PATH, 'last_seq_reversed_15s_10seq.csv'))
